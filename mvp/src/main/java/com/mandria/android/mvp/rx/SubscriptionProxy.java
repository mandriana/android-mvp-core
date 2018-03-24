package com.mandria.android.mvp.rx;

import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.ReplaySubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Proxy for the original observable subscription.
 * A {@link ReplaySubject} subscribes to the observable and is used
 * to attach the {@link rx.Observer}
 */
class SubscriptionProxy<View, Result> {

    private final Subscription mReplaySubscription;

    private final CompositeSubscription mSubscriptionList;

    private Observable<BoundData<View, Result>> mObservable;

    private Subscription mSubscription;

    /**
     * Constructor.
     *
     * @param observable  Original observable.
     * @param view        Observable that emits the view.
     */
    SubscriptionProxy(Observable<Result> observable, Observable<View> view) {
        // Creates a replay subject which will subscribe to the observable.
        ReplaySubject<Result> replaySubject = ReplaySubject.create();
        mReplaySubscription = observable.subscribe(replaySubject);

        // Keeps as the observable reference the replaySubject
        // so that the original observable can continue its work
        // and we can unsubscribe from the replaySubject
        mObservable = Observable
                .combineLatest(
                        view,
                        replaySubject.materialize(),
                        new Func2<View, Notification<Result>, BoundData<View, Result>>() {
                            @Override
                            public BoundData<View, Result> call(View v, Notification<Result> replayNotification) {
                                // In case view is emitted as null
                                // we unsubscribe from the replay subject
                                // to avoid passing null view (view is detached)
                                if (v == null) {
                                    if (mSubscription != null) {
                                        unsubscribe();
                                    }
                                    return null;
                                }

                                return new BoundData<>(v, replayNotification);
                            }
                        })
                .filter(new Func1<BoundData<View, Result>, Boolean>() {
                    @Override
                    public Boolean call(BoundData<View, Result> vtBoundData) {
                        return vtBoundData != null;
                    }
                });

        // Adds the replaySubject subscription to the CompositeSubscription
        // to be able to unsubscribe the replaySubject from the original observable
        mSubscriptionList = new CompositeSubscription(mReplaySubscription);
    }

    /**
     * Subscribes to the observable and stores the subscription.
     *
     * @param observer Observer.
     * @return The subscription of the observer to the observable.
     */
    Subscription subscribe(Action1<BoundData<View, Result>> observer) {
        unsubscribe();
        mSubscription = mObservable.subscribe(observer);
        mSubscriptionList.add(mSubscription);
        return mSubscription;
    }

    /**
     * Unsubscribe to the observable.
     */
    void unsubscribe() {
        if (mSubscription != null) {
            mSubscriptionList.remove(mSubscription);
        }
    }

    /**
     * Gets if the subscription to the replaySubject is unsubscribed.
     *
     * @return True if unsubscribed.
     */
    boolean isUnsubscribed() {
        return mSubscription != null && mSubscription.isUnsubscribed();
    }

    /**
     * Cancels the observable by unsubscribing all subscriptions
     * (including the one from the replaySubject).
     */
    void cancel() {
        mSubscriptionList.unsubscribe();
    }

    /**
     * Gets if the observable is canceled.
     *
     * @return True if no subscription remains.
     */
    boolean isCanceled() {
        return isUnsubscribed() && mReplaySubscription.isUnsubscribed();
    }
}
