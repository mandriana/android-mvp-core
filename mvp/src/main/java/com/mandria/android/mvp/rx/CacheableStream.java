package com.mandria.android.mvp.rx;

import com.mandria.android.mvp.rx.proxies.AbstractSubscriptionProxy;
import com.mandria.android.mvp.rx.proxies.FlowableSubscriptionProxy;
import com.mandria.android.mvp.rx.proxies.ObservableSubscriptionProxy;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

/**
 * This class is used to cache a stream with its subscriber.
 */
public class CacheableStream<View, Result> {

    private AbstractSubscriptionProxy<View, Result> mProxy;

    private MVPConsumer<View, Result> mConsumer;

    /**
     * Constructor.
     *
     * @param observable Observable to cache.
     * @param view       Observable that emits the view.
     * @param consumer   Consumer to attach to the observable.
     */
    CacheableStream(Observable<Result> observable, Observable<RxView<View>> view, MVPConsumer<View, Result> consumer) {
        mProxy = new ObservableSubscriptionProxy<>(observable, view);
        mConsumer = consumer;
    }

    /**
     * Constructor.
     *
     * @param flowable Flowable to cache.
     * @param view     Observable that emits the view.
     * @param consumer Consumer to attach to the observable.
     */
    CacheableStream(Flowable<Result> flowable, Observable<RxView<View>> view, MVPConsumer<View, Result> consumer) {
        mProxy = new FlowableSubscriptionProxy<>(flowable, view);
        mConsumer = consumer;
    }

    /**
     * Constructor.
     *
     * @param single   Single to cache.
     * @param view     Observable that emits the view.
     * @param consumer Consumer to attach to the observable.
     */
    CacheableStream(Single<Result> single, Observable<RxView<View>> view, MVPConsumer<View, Result> consumer) {
        mProxy = new FlowableSubscriptionProxy<>(single.toFlowable(), view);
        mConsumer = consumer;
    }

    /**
     * Constructor.
     *
     * @param completable Completable to cache.
     * @param view        Observable that emits the view.
     * @param consumer    Consumer to attach to the observable.
     */
    CacheableStream(Completable completable, Observable<RxView<View>> view, MVPConsumer<View, Result> consumer) {
        mProxy = new ObservableSubscriptionProxy<>(completable.<Result>toObservable(), view);
        mConsumer = consumer;
    }

    /**
     * Constructor.
     *
     * @param maybe    Maybe to cache.
     * @param view     Observable that emits the view.
     * @param consumer Consumer to attach to the observable.
     */
    CacheableStream(Maybe<Result> maybe, Observable<RxView<View>> view, MVPConsumer<View, Result> consumer) {
        mProxy = new ObservableSubscriptionProxy<>(maybe.toObservable(), view);
        mConsumer = consumer;
    }

    /**
     * Resumes the observable.
     */
    void resume() {
        mProxy.subscribe(mConsumer);
    }

    /**
     * Disposes from the observable.
     */
    void dispose() {
        mProxy.dispose();
    }

    /**
     * Cancels the observable.
     */
    void cancel() {
        mProxy.cancel();
    }

    /**
     * Test method is used only to return the consumer and offer the possibility to trigger
     * callbacks
     * @return the consumer
     */
    public MVPConsumer<View,Result> test() {
        cancel();
        return mConsumer;
    }
}
