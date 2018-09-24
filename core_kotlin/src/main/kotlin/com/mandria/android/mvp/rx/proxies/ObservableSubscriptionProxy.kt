package com.mandria.android.mvp.rx.proxies

import com.mandria.android.mvp.rx.BoundData
import com.mandria.android.mvp.rx.RxView
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.observers.DisposableObserver
import io.reactivex.processors.ReplayProcessor
import io.reactivex.subjects.ReplaySubject

/**
 * Proxy for the original [Observable] subscription.
 * A [ReplayProcessor] subscribes to the observable and is used to attach the [Consumer] from the
 * [subscribe] method.
 *
 * @param originalFlowable The original stream to consume.
 * @param view The view subject as observable.
 */
class ObservableSubscriptionProxy<View, Result>(
    originalObservable: Observable<Result>, view: Observable<RxView<View>>
) : AbstractSubscriptionProxy<View, Result>() {

    private val replayDisposable: DisposableObserver<Result>

    private val observable: Observable<BoundData<View, Result>>

    init {
        // Creates a replay subject which will subscribe to the observable.
        val replaySubject = ReplaySubject.create<Result>()

        replayDisposable = object : DisposableObserver<Result>() {
            override fun onNext(result: Result) {
                replaySubject.onNext(result)
            }

            override fun onError(e: Throwable) {
                replaySubject.onError(e)
            }

            override fun onComplete() {
                replaySubject.onComplete()
            }
        }
        originalObservable.subscribe(replayDisposable)

        // Keeps as the observable reference the combination of the view behaviour subject and the replay subject
        // so that the original observable can continue its work and we can dispose from the replay subject.
        this.observable = Observable
            .combineLatest(view, replaySubject.materialize(), combineFunction)
            .filter(filterPredicate)

        // Adds the replaySubject subscription to the CompositeSubscription
        // to be able to dispose the replaySubject from the original observable.
        compositeDisposable.add(replayDisposable)
    }

    override fun subscribe(consumer: Consumer<BoundData<View, Result>>): Disposable {
        dispose()

        return observable.subscribe().apply {
            disposable = this
            compositeDisposable.add(this)
        }
    }

    override fun dispose() {
        disposable?.let { compositeDisposable.remove(it) }
    }

    /** Gets if the observable is canceled. **/
    fun isCanceled(): Boolean = isDisposed() && replayDisposable.isDisposed
}