package com.mandria.android.mvp.rx.proxies

import com.mandria.android.mvp.rx.BoundData
import com.mandria.android.mvp.rx.RxView
import io.reactivex.Notification
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.functions.Predicate

/** An abstract class to manipulate subscription proxies. **/
abstract class AbstractSubscriptionProxy<View, Result> {

    /** Combine latest bi-function to apply. **/
    val combineFunction: BiFunction<RxView<View>, Notification<Result>, BoundData<View, Result>>

    /** Predicate to filter item emitted by the combineLatest bi-function. **/
    val filterPredicate: Predicate<BoundData<View, Result>>

    /** Composite disposable to retain replaySubject subscription and combination subscription. **/
    val compositeDisposable: CompositeDisposable = CompositeDisposable()

    /** Disposable acquired from the combination subscription. **/
    var disposable: Disposable? = null

    init {
        combineFunction = BiFunction { rxView, replayNotification ->
            // In case view is emitted as null
            // we dispose from the replay subject
            // to avoid passing null view (view is detached)
            if (rxView.view == null)
                disposable?.let {
                    dispose()
                }

            BoundData(rxView.view, replayNotification)
        }
        filterPredicate = Predicate { it.view != null }
    }

    /** Cancels the stream disposing all disposables. **/
    fun cancel() {
        compositeDisposable.dispose()
    }

    /** Gets if the disposable attached to the replaySubject is disposed. **/
    fun isDisposed(): Boolean = disposable?.isDisposed ?: false

    /**
     * Subscribes to the stream using the given [consumer].
     * Return the disposable from the subscription.
     */
    abstract fun subscribe(consumer: Consumer<BoundData<View, Result>>): Disposable

    /** Disposes from the stream. **/
    abstract fun dispose()
}