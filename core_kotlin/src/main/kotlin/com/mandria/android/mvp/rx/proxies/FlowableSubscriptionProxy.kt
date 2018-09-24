package com.mandria.android.mvp.rx.proxies

import com.mandria.android.mvp.rx.BoundData
import com.mandria.android.mvp.rx.RxView
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.processors.ReplayProcessor
import io.reactivex.subscribers.DisposableSubscriber

/**
 * Proxy for the original [Flowable] subscription.
 * A [ReplayProcessor] subscribes to the flowable and is used to attach the [Consumer] from the
 * [subscribe] method.
 *
 * @param originalFlowable The original stream to consume.
 * @param view The view subject as observable.
 */
class FlowableSubscriptionProxy<View, Result>(
    originalFlowable: Flowable<Result>,
    view: Observable<RxView<View>>
) : AbstractSubscriptionProxy<View, Result>() {

    private val replayDisposable: DisposableSubscriber<Result>

    private val flowable: Flowable<BoundData<View, Result>>

    init {
        // Creates a replay processor which will subscribe to the flowable.
        val replayProcessor = ReplayProcessor.create<Result>()

        replayDisposable = object : DisposableSubscriber<Result>() {
            override fun onNext(result: Result) {
                replayProcessor.onNext(result)
            }

            override fun onError(e: Throwable) {
                replayProcessor.onError(e)
            }

            override fun onComplete() {
                replayProcessor.onComplete()
            }
        }
        originalFlowable.subscribe(replayDisposable)

        // Keeps as the flowable reference the combination of the view behaviour subject and the replay processor
        // so that the original flowable can continue its work and we can dispose from the replay processor.
        // View is converted to flowable with BackpressureStrategy.LATEST, since only the latest emission interests us.
        flowable = Flowable
            .combineLatest(
                view.toFlowable(BackpressureStrategy.LATEST),
                replayProcessor.materialize(),
                combineFunction
            )
            .filter(filterPredicate)

        // Adds the replayProcessor subscription to the CompositeSubscription
        // to be able to dispose the replayProcessor from the original flowable.
        compositeDisposable.add(replayDisposable)
    }

    override fun subscribe(consumer: Consumer<BoundData<View, Result>>): Disposable {
        dispose()

        return flowable.subscribe(consumer).also {
            disposable = it
            compositeDisposable.add(it)
        }
    }

    override fun dispose() {
        disposable?.let { compositeDisposable.remove(it) }
    }

    /** Gets if the observable is canceled. **/
    fun isCanceled(): Boolean = isDisposed() && replayDisposable.isDisposed
}