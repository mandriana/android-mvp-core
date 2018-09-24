package com.mandria.android.mvp.rx

import com.mandria.android.mvp.rx.proxies.AbstractSubscriptionProxy
import com.mandria.android.mvp.rx.proxies.FlowableSubscriptionProxy
import com.mandria.android.mvp.rx.proxies.ObservableSubscriptionProxy
import io.reactivex.*
import io.reactivex.functions.Consumer

/** This class is used to cache a stream with its subscriber. **/
class CacheableStream<View, Result> {

    /** Proxy subscription to replay results if needed. **/
    private val proxy: AbstractSubscriptionProxy<View, Result>

    /** Consumer to attach to the stream. **/
    private val consumer: Consumer<BoundData<View, Result>>

    /**
     * Constructor.
     *
     * @param observable Observable to cache.
     * @param view       Observable that emits the view.
     * @param consumer   Consumer to attach to the observable.
     */
    constructor(
        observable: Observable<Result>,
        view: Observable<RxView<View>>,
        consumer: Consumer<BoundData<View, Result>>
    ) {
        proxy = ObservableSubscriptionProxy(observable, view)
        this.consumer = consumer
    }

    /**
     * Constructor.
     *
     * @param flowable Flowable to cache.
     * @param view     Observable that emits the view.
     * @param consumer Consumer to attach to the observable.
     */
    constructor(
        flowable: Flowable<Result>,
        view: Observable<RxView<View>>,
        consumer: Consumer<BoundData<View, Result>>
    ) {
        proxy = FlowableSubscriptionProxy(flowable, view)
        this.consumer = consumer
    }

    /**
     * Constructor.
     *
     * @param single   Single to cache.
     * @param view     Observable that emits the view.
     * @param consumer Consumer to attach to the observable.
     */
    constructor(
        single: Single<Result>,
        view: Observable<RxView<View>>,
        consumer: Consumer<BoundData<View, Result>>
    ) {
        proxy = FlowableSubscriptionProxy(single.toFlowable(), view)
        this.consumer = consumer
    }

    /**
     * Constructor.
     *
     * @param completable Completable to cache.
     * @param view        Observable that emits the view.
     * @param consumer    Consumer to attach to the observable.
     */
    constructor(
        completable: Completable,
        view: Observable<RxView<View>>,
        consumer: Consumer<BoundData<View, Result>>
    ) {
        proxy = ObservableSubscriptionProxy(completable.toObservable(), view)
        this.consumer = consumer
    }

    /**
     * Constructor.
     *
     * @param maybe    Maybe to cache.
     * @param view     Observable that emits the view.
     * @param consumer Consumer to attach to the observable.
     */
    constructor(
        maybe: Maybe<Result>,
        view: Observable<RxView<View>>,
        consumer: Consumer<BoundData<View, Result>>
    ) {
        proxy = ObservableSubscriptionProxy(maybe.toObservable(), view)
        this.consumer = consumer
    }

    /** Resumes the stream. **/
    fun resume() {
        proxy.subscribe(consumer)
    }

    /** Disposes from the stream. **/
    fun dispose() {
        proxy.dispose()
    }

    /** Cancels the stream. **/
    fun cancel() {
        proxy.cancel()
    }
}