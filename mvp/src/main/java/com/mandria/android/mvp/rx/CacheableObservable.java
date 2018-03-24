package com.mandria.android.mvp.rx;

import rx.Observable;
import rx.functions.Action1;

/**
 * This class is used to cache an observable with its subscriber.
 */
class CacheableObservable<View, Result> {

    private SubscriptionProxy<View, Result> mProxy;

    private Action1<BoundData<View, Result>> mSubscriber;

    /**
     * Constructor.
     *
     * @param observable  Observable to cache.
     * @param view        Observable that emits the view.
     * @param subscriber  Subscriber to attach to the observable.
     */
    CacheableObservable(Observable<Result> observable, Observable<View> view,
            Action1<BoundData<View, Result>> subscriber) {
        mProxy = new SubscriptionProxy<>(observable, view);
        mSubscriber = subscriber;
    }

    /**
     * Resumes the observable.
     */
    void resume() {
        mProxy.subscribe(mSubscriber);
    }

    /**
     * Unsubscribes from the observable.
     */
    void unsubscribe() {
        mProxy.unsubscribe();
    }

    /**
     * Cancels the observable.
     */
    void cancel() {
        mProxy.cancel();
    }
}
