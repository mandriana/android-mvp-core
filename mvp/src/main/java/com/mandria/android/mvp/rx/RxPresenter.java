package com.mandria.android.mvp.rx;

import com.mandria.android.mvp.MVPLogger;
import com.mandria.android.mvp.Presenter;
import com.mandria.android.mvp.rx.callbacks.OnCompleted;
import com.mandria.android.mvp.rx.callbacks.OnError;
import com.mandria.android.mvp.rx.callbacks.OnNext;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * A base presenter to use with RxJava features.
 */
public class RxPresenter<V> extends Presenter<V> {

    private final String mTag = getClass().getSimpleName();

    /**
     * Behavior subject to fire the cache removal off all awaiting task.
     */
    private final BehaviorSubject<Boolean> mCacheSynchronization = BehaviorSubject.create();

    /**
     * Queue to store the observable tags which have terminated while manipulating the cache.
     * This list is used to avoid ConcurrentModificationException on the cache.
     */
    private final List<String> mTerminatedQueue = new ArrayList<>();

    /**
     * Behaviour subject to publish the view state through observable operations
     */
    private final BehaviorSubject<V> mView = BehaviorSubject.create();

    /**
     * Map of cached observables.
     * Operations on this map should be synchronized to avoid concurrency access.
     */
    private final ConcurrentHashMap<String, CacheableObservable<V, ?>> mCache = new ConcurrentHashMap<>();

    /**
     * Stores the subscriptions to release them in {@link #destroy()} call.
     */
    private CompositeSubscription mSubscriptions;

    /**
     * Actions queue.
     */
    private final HashMap<String, Action1<V>> mQueue = new HashMap<>();

    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        mSubscriptions = new CompositeSubscription();
        mCacheSynchronization.subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean manipulating) {
                if (!manipulating && !mTerminatedQueue.isEmpty()) {
                    ListIterator<String> listIterator = mTerminatedQueue.listIterator();
                    while (listIterator.hasNext()) {
                        String tag = listIterator.next();
                        removeFromCache(tag);
                        listIterator.remove();
                    }
                }
            }
        });
    }

    @CallSuper
    @Override
    protected void onViewAttached(V view) {
        super.onViewAttached(view);

        mView.onNext(view);
        resumeQueue(view);
        resumeAll();
    }

    @CallSuper
    @Override
    protected void onViewDetached() {
        super.onViewDetached();

        mView.onNext(null);
        unsubscribeAll();
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();

        mView.onCompleted();
        mCacheSynchronization.onCompleted();
        cancelAll();
    }

    /**
     * Unsubscribes from all subscribed observables.
     */
    private void unsubscribeAll() {
        if (mSubscriptions != null) {
            mSubscriptions.clear();
            mSubscriptions = new CompositeSubscription();
        }

        mCacheSynchronization.onNext(true);

        for (String key : mCache.keySet()) {
            mCache.get(key).unsubscribe();
        }

        mCacheSynchronization.onNext(false);
    }

    /**
     * Cancels all running observables by unsubscribing to them
     * and clearing the cache.
     */
    protected final void cancelAll() {
        if (mSubscriptions != null) {
            mSubscriptions.clear();
            mSubscriptions = new CompositeSubscription();
        }

        mCacheSynchronization.onNext(true);

        for (String key : mCache.keySet()) {
            mCache.get(key).cancel();
        }
        mCache.clear();

        mCacheSynchronization.onNext(false);
    }

    /**
     * Resumes all cached observables.
     */
    private void resumeAll() {
        mCacheSynchronization.onNext(true);

        for (String key : mCache.keySet()) {
            mCache.get(key).resume();
        }

        mCacheSynchronization.onNext(false);
    }

    /**
     * Cancels a cached observable.
     *
     * @param tag Cached observable tag.
     */
    protected void cancel(String tag) {
        CacheableObservable<V, ?> cached = mCache.get(tag);
        if (cached != null) {
            cached.cancel();
            mCache.remove(tag);
        }
    }

    /**
     * Removes an observable from the cache.
     *
     * @param tag Observable tag to remove.
     */
    private void removeFromCache(String tag) {
        MVPLogger.d(mTag, String.format("Remove %s from cache", tag));
        mCache.remove(tag);
    }

    /**
     * Every observable subscription should be added here in order to avoid memory leak.
     * The subscriptions will be unsubscribed in {@link #onViewDetached()} callback.
     *
     * @param subscription Observable subscription.
     */
    public void addSubscription(Subscription subscription) {
        mSubscriptions.add(subscription);
    }

    /**
     * Removes a previously registered subscription.
     *
     * @param subscription Subscription to remove.
     */
    public void removeSubscription(Subscription subscription) {
        mSubscriptions.remove(subscription);
    }

    /**
     * <p>
     * Starts an observable.
     * </p>
     * <p>
     * If an existing observable with the same tag exists in cache, the observable will be resumed.
     * Otherwise it will be added in the cached and started.
     * </p>
     * <p>
     * The withDefaultSchedulers parameter is used to attach or not the observable to default schedulers ({@link
     * RxUtils#applyIOScheduler}).
     * </p>
     *
     * @param tag                   Observable tag.
     * @param observable            Observable to start.
     * @param withDefaultSchedulers True if default schedulers should be applied.
     * @param onNext                OnNext action to call
     * @param onError               OnError action to call
     * @param onCompleted           OnCompleted action to call
     * @param <Result>              Result type of the observable.
     */
    public <Result> void start(@NonNull final String tag, @NonNull Observable<Result> observable, boolean withDefaultSchedulers,
            @Nullable final OnNext<V, Result> onNext, @Nullable final OnError<V> onError, @Nullable final OnCompleted<V> onCompleted) {

        // noinspection unchecked
        CacheableObservable<V, Result> cached = (CacheableObservable<V, Result>) mCache.get(tag);

        if (!mCache.containsKey(tag)) {
            MVPLogger.d(mTag, String.format("Starting task : %s", tag));
            if (withDefaultSchedulers) {
                observable = observable.compose(RxUtils.<Result>applyIOScheduler());
            }
            cached = new CacheableObservable<>(
                    observable,
                    mView,
                    new Action1<BoundData<V, Result>>() {
                        @Override
                        public void call(BoundData<V, Result> vResultBoundData) {
                            V view = vResultBoundData.getView();
                            Notification<Result> notification = vResultBoundData.getData();

                            if (onNext != null && notification.isOnNext()) {
                                onNext.call(view, notification.getValue());
                            } else if (onCompleted != null && notification.isOnCompleted()) {
                                onCompleted.call(view);
                            } else if (onError != null && notification.isOnError()) {
                                onError.call(view, notification.getThrowable());
                            }

                            if (notification.isOnCompleted() || notification.isOnError()) {
                                // Previous onTerminate action is executed here because at this point the view
                                // had received the notification, no need to deal with backpressure
                                if (mCacheSynchronization.getValue()) {
                                    mTerminatedQueue.add(tag);
                                } else {
                                    removeFromCache(tag);
                                }
                            }
                        }
                    });

            if (!mCache.containsKey(tag)) {
                mCache.put(tag, cached);
            } else {
                cached.cancel();
                // noinspection unchecked
                cached = (CacheableObservable<V, Result>) mCache.get(tag);
            }
        } else {
            MVPLogger.d(mTag, String.format("Resuming task : %s", tag));
        }

        if (cached != null) {
            cached.resume();
        }
    }

    /**
     * <p>
     * Shortcut for {@link #start(String, Observable, boolean, OnNext, OnError, OnCompleted)} method but with no {@link OnCompleted}
     * action.
     * </p>
     * <p>
     * Observable are attached with default schedulers ({@link RxUtils#applyIOScheduler}).
     * </p>
     *
     * @param tag         Observable tag.
     * @param observable  Observable to start.
     * @param onNext      OnNext action to call
     * @param onError     OnError action to call
     * @param onCompleted OnCompleted action to call
     * @param <Result>    Result type of the observable.
     */
    public <Result> void start(@NonNull final String tag, @NonNull Observable<Result> observable, @Nullable final OnNext<V, Result> onNext,
            @Nullable final OnError<V> onError, @Nullable final OnCompleted<V> onCompleted) {
        start(tag, observable, true, onNext, onError, onCompleted);
    }

    /**
     * Shortcut for {@link #start(String, Observable, OnNext, OnError, OnCompleted)} method but with no {@link OnCompleted} action.<br />
     *
     * @param tag        Observable tag.
     * @param observable Observable to start.
     * @param onNext     OnNext action to call
     * @param onError    OnError action to call
     * @param <Result>   Result type of the observable.
     */
    public <Result> void start(@NonNull final String tag, @NonNull Observable<Result> observable, @Nullable final OnNext<V, Result> onNext,
            @Nullable final OnError<V> onError) {
        start(tag, observable, onNext, onError, null);
    }

    /**
     * Calls the action once view is attached.
     * The tag is used to remove the observable from the task queue if not started yet.
     * In case it already started, calls {@link #cancel(String)} with the given tag, i.e. the tag parameter
     * should be the same as the one used with {@link #start} methods (observable tag).
     * This is intended, for instance, to be used when {@link android.app.Activity#onRequestPermissionsResult(int, String[], int[])} need to
     * start a task once view is attached.
     *
     * @param tag    Action tag (ideally same as observable tag if an observable should be started in the action0 param).
     * @param action Action to call once view is attached.
     */
    public void startOnViewAttached(final String tag, final Action1<V> action) {
        if (mView.getValue() != null) {
            try {
                action.call(mView.getValue());
            } catch (Exception e) {
                MVPLogger.e(mTag, e.getMessage());
            }
        } else {
            mQueue.put(tag, action);
        }
    }

    /**
     * Removes the action from the queue with the ability to cancel a task that as started with the same tag.
     *
     * @param tag             Action tag (ideally same as observable tag if an observable should be started in the action0 param).
     * @param cancelIfStarted Tries to cancel the task if any matching the given tag.
     */
    public void cancelWaitingForViewAttached(String tag, boolean cancelIfStarted) {
        mQueue.remove(tag);
        if (cancelIfStarted) {
            cancel(tag);
        }
    }

    /**
     * Resumes the queue.
     *
     * @param view The view attached to the presenter.
     */
    private void resumeQueue(V view) {
        if (!mQueue.isEmpty()) {
            MVPLogger.d(mTag, String.format("%s action waited for view attached to start", mQueue.size()));
            Iterator<Map.Entry<String, Action1<V>>> queueIterator = mQueue.entrySet().iterator();
            while (queueIterator.hasNext()) {
                Map.Entry<String, Action1<V>> next = queueIterator.next();
                MVPLogger.d(mTag, String.format("Calling action for tag : %s", next.getKey()));
                next.getValue().call(view);
                queueIterator.remove();
            }
        }
    }

    /**
     * Gets if a task is still running by checking if it exists in the cache.
     *
     * @param tag Task tag.
     * @return True if task is in progress else false.
     */
    public boolean isTaskInProgress(String tag) {
        return mCache.containsKey(tag);
    }
}
