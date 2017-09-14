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

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;


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
     * Behaviour subject to publish the view state through observable operations.
     * View state is wrapped in a {@link RxView} since BehaviorSubject cannot emit null.
     */
    private final BehaviorSubject<RxView<V>> mView = BehaviorSubject.create();

    /**
     * Map of cached observables.
     * Operations on this map should be synchronized to avoid concurrency access.
     */
    private final HashMap<String, CacheableStream<V, ?>> mCache = new HashMap<>();

    /**
     * Stores the subscriptions to release them in {@link #destroy()} call.
     */
    private CompositeDisposable mDisposables;

    /**
     * Actions queue.
     */
    private final HashMap<String, Consumer<V>> mQueue = new HashMap<>();

    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        mDisposables = new CompositeDisposable();
        mCacheSynchronization.subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean manipulating) {
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

        mView.onNext(new RxView<>(view));
        resumeQueue(view);
        resumeAll();
    }

    @CallSuper
    @Override
    protected void onViewDetached() {
        super.onViewDetached();

        mView.onNext(new RxView<V>(null));
        disposeAll();
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();

        mView.onComplete();
        mCacheSynchronization.onComplete();
        cancelAll();
    }

    /**
     * Disposes from all subscribed observables.
     */
    private void disposeAll() {
        if (mDisposables != null) {
            mDisposables.clear();
            mDisposables = new CompositeDisposable();
        }

        mCacheSynchronization.onNext(true);

        for (String key : mCache.keySet()) {
            mCache.get(key).dispose();
        }

        mCacheSynchronization.onNext(false);
    }

    /**
     * Cancels all running observables by disposing from them and clearing the cache.
     */
    private void cancelAll() {
        if (mDisposables != null) {
            mDisposables.clear();
            mDisposables = new CompositeDisposable();
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
        CacheableStream<V, ?> cached = mCache.get(tag);
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
     * Every disposable should be added here in order to avoid memory leak.
     * The subscriptions will be unsubscribed in {@link #onViewDetached()} callback.
     *
     * @param disposable Observable subscription.
     */
    public void addSubscription(Disposable disposable) {
        mDisposables.add(disposable);
    }

    /**
     * Removes a previously registered disposable.
     *
     * @param disposable Subscription to remove.
     */
    public void removeSubscription(Disposable disposable) {
        mDisposables.remove(disposable);
    }

    /**
     * Gets the action to run on each {@link CacheableStream} to remove stream from cache or queue.
     *
     * @param tag Stream tag.
     * @return The action to run.
     */
    private Action getCacheableOnTerminateAction(final String tag) {
        return new Action() {
            @Override
            public void run() throws Exception {
                if (mCacheSynchronization.getValue()) {
                    mTerminatedQueue.add(tag);
                } else {
                    removeFromCache(tag);
                }
            }
        };
    }

    /**
     * Gets a consumer for the stream which will dispatch events to each corresponding callback.
     *
     * @param onNext      OnNext action to call
     * @param onError     OnError action to call
     * @param onCompleted OnCompleted action to call
     * @param <Result>    Result type of the observable.
     * @return The consumer to attach to the stream.
     */
    private <Result> Consumer<BoundData<V, Result>> getCacheableStreamConsumer(@Nullable final OnNext<V, Result> onNext,
            @Nullable final OnError<V> onError, @Nullable final OnCompleted<V> onCompleted) {
        return new Consumer<BoundData<V, Result>>() {
            @Override
            public void accept(@io.reactivex.annotations.NonNull BoundData<V, Result> rxViewResultBoundData) throws Exception {
                V view = rxViewResultBoundData.getView();
                Notification<Result> notification = rxViewResultBoundData.getData();

                if (onNext != null && notification.isOnNext()) {
                    onNext.accept(view, notification.getValue());
                } else if (onCompleted != null && notification.isOnComplete()) {
                    onCompleted.accept(view);
                } else if (onError != null && notification.isOnError()) {
                    onError.accept(view, notification.getError());
                }
            }
        };
    }

    /**
     * Calls the action once view is attached.
     * The tag is used to remove the observable from the task queue if not started yet.
     * In case it already started, calls {@link #cancel(String)} with the given tag, i.e. the tag parameter
     * should be the same as the one used with {@link #start} methods (observable tag).
     * This is intended to be used when {@link android.app.Activity#onRequestPermissionsResult(int, String[], int[])} need to
     * start a task once view is attached.
     *
     * @param tag    Action tag (ideally same as observable tag if an observable should be started in the action0 param).
     * @param consumer Consumer to call once view is attached.
     */
    public void startOnViewAttached(final String tag, final Consumer<V> consumer) {
        if (mView.getValue() != null && mView.getValue().view != null) {
            try {
                consumer.accept(mView.getValue().view);
            } catch (Exception e) {
                MVPLogger.e(mTag, e.getMessage());
            }
        } else {
            mQueue.put(tag, consumer);
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
     */
    private void resumeQueue(V view) {
        if (!mQueue.isEmpty()) {
            MVPLogger.d(mTag, String.format("%s action waited for view attached to start", mQueue.size()));
            Iterator<Map.Entry<String, Consumer<V>>> queueIterator = mQueue.entrySet().iterator();
            while (queueIterator.hasNext()) {
                Map.Entry<String, Consumer<V>> next = queueIterator.next();
                try {
                    MVPLogger.d(mTag, String.format("Calling action for tag : %s", next.getKey()));
                    next.getValue().accept(view);
                } catch (Exception e) {
                    MVPLogger.e(mTag, e.getMessage());
                }
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

    /**
     * <p>
     * Starts an observable.
     * </p>
     * <p>
     * If an existing observable with the same tag exists in cache, the observable will be resumed.
     * Otherwise it will be added in the cache and started.
     * </p>
     * <p>
     * The withDefaultSchedulers parameter is used to attach or not the observable to default schedulers ({@link
     * RxUtils#observableIOSchedulerTransformer}).
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
        CacheableStream<V, Result> cached = (CacheableStream<V, Result>) mCache.get(tag);

        if (!mCache.containsKey(tag)) {
            mCache.put(tag, null);

            MVPLogger.d(mTag, String.format("Starting task : %s", tag));
            if (withDefaultSchedulers) {
                observable = observable.compose(RxUtils.<Result>applyObservableIOScheduler());
            }
            cached = new CacheableStream<>(
                    observable,
                    mView,
                    getCacheableOnTerminateAction(tag),
                    getCacheableStreamConsumer(onNext, onError, onCompleted));

            mCache.put(tag, cached);
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
     * Observable are attached with default schedulers ({@link RxUtils#observableIOSchedulerTransformer}).
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
     * <p>
     * Starts a flowable.
     * </p>
     * <p>
     * If an existing flowable with the same tag exists in cache, the flowable will be resumed.
     * Otherwise it will be added in the cache and started.
     * </p>
     * <p>
     * The withDefaultSchedulers parameter is used to attach or not the flowable to default schedulers ({@link
     * RxUtils#applyFlowableIOScheduler()}).
     * </p>
     *
     * @param tag                   Flowable tag.
     * @param flowable              Flowable to start.
     * @param withDefaultSchedulers True if default schedulers should be applied.
     * @param onNext                OnNext action to call
     * @param onError               OnError action to call
     * @param onCompleted           OnCompleted action to call
     * @param <Result>              Result type of the flowable.
     */
    public <Result> void start(@NonNull final String tag, @NonNull Flowable<Result> flowable, boolean withDefaultSchedulers,
            @Nullable final OnNext<V, Result> onNext, @Nullable final OnError<V> onError, @Nullable final OnCompleted<V> onCompleted) {

        // noinspection unchecked
        CacheableStream<V, Result> cached = (CacheableStream<V, Result>) mCache.get(tag);

        if (!mCache.containsKey(tag)) {
            mCache.put(tag, null);

            MVPLogger.d(mTag, String.format("Starting task : %s", tag));
            if (withDefaultSchedulers) {
                flowable = flowable.compose(RxUtils.<Result>applyFlowableIOScheduler());
            }
            cached = new CacheableStream<>(
                    flowable,
                    mView,
                    getCacheableOnTerminateAction(tag),
                    getCacheableStreamConsumer(onNext, onError, onCompleted));

            mCache.put(tag, cached);
        } else {
            MVPLogger.d(mTag, String.format("Resuming task : %s", tag));
        }

        if (cached != null) {
            cached.resume();
        }
    }

    /**
     * <p>
     * Shortcut for {@link #start(String, Flowable, boolean, OnNext, OnError, OnCompleted)} method but with no {@link OnCompleted}
     * action.
     * </p>
     * <p>
     * Observable are attached with default schedulers ({@link RxUtils#applyFlowableIOScheduler()}).
     * </p>
     *
     * @param tag         Flowable tag.
     * @param flowable    Flowable to start.
     * @param onNext      OnNext action to call
     * @param onError     OnError action to call
     * @param onCompleted OnCompleted action to call
     * @param <Result>    Result type of the flowable.
     */
    public <Result> void start(@NonNull final String tag, @NonNull Flowable<Result> flowable, @Nullable final OnNext<V, Result> onNext,
            @Nullable final OnError<V> onError, @Nullable final OnCompleted<V> onCompleted) {
        start(tag, flowable, true, onNext, onError, onCompleted);
    }

    /**
     * Shortcut for {@link #start(String, Flowable, OnNext, OnError, OnCompleted)} method but with no {@link OnCompleted} action.<br />
     *
     * @param tag      Flowable tag.
     * @param flowable Flowable to start.
     * @param onNext   OnNext action to call
     * @param onError  OnError action to call
     * @param <Result> Result type of the flowable.
     */
    public <Result> void start(@NonNull final String tag, @NonNull Flowable<Result> flowable, @Nullable final OnNext<V, Result> onNext,
            @Nullable final OnError<V> onError) {
        start(tag, flowable, onNext, onError, null);
    }

    /**
     * <p>
     * Starts a single.
     * </p>
     * <p>
     * If an existing single with the same tag exists in cache, the single will be resumed.
     * Otherwise it will be added in the cache and started.
     * </p>
     * <p>
     * The withDefaultSchedulers parameter is used to attach or not the single to default schedulers ({@link
     * RxUtils#applySingleIOScheduler()}).
     * </p>
     *
     * @param tag                   Single tag.
     * @param single                Single to start.
     * @param withDefaultSchedulers True if default schedulers should be applied.
     * @param onNext                OnNext action to call
     * @param onError               OnError action to call
     * @param onCompleted           OnCompleted action to call
     * @param <Result>              Result type of the single.
     */
    public <Result> void start(@NonNull final String tag, @NonNull Single<Result> single, boolean withDefaultSchedulers,
            @Nullable final OnNext<V, Result> onNext, @Nullable final OnError<V> onError, @Nullable final OnCompleted<V> onCompleted) {

        // noinspection unchecked
        CacheableStream<V, Result> cached = (CacheableStream<V, Result>) mCache.get(tag);

        if (!mCache.containsKey(tag)) {
            mCache.put(tag, null);

            MVPLogger.d(mTag, String.format("Starting task : %s", tag));
            if (withDefaultSchedulers) {
                single = single.compose(RxUtils.<Result>applySingleIOScheduler());
            }
            cached = new CacheableStream<>(
                    single,
                    mView,
                    getCacheableOnTerminateAction(tag),
                    getCacheableStreamConsumer(onNext, onError, onCompleted));

            mCache.put(tag, cached);
        } else {
            MVPLogger.d(mTag, String.format("Resuming task : %s", tag));
        }

        if (cached != null) {
            cached.resume();
        }
    }

    /**
     * <p>
     * Shortcut for {@link #start(String, Single, boolean, OnNext, OnError, OnCompleted)} method but with no {@link OnCompleted}
     * action.
     * </p>
     * <p>
     * Observable are attached with default schedulers ({@link RxUtils#applySingleIOScheduler()}).
     * </p>
     *
     * @param tag         Single tag.
     * @param single      Single to start.
     * @param onNext      OnNext action to call
     * @param onError     OnError action to call
     * @param onCompleted OnCompleted action to call
     * @param <Result>    Result type of the single.
     */
    public <Result> void start(@NonNull final String tag, @NonNull Single<Result> single, @Nullable final OnNext<V, Result> onNext,
            @Nullable final OnError<V> onError, @Nullable final OnCompleted<V> onCompleted) {
        start(tag, single, true, onNext, onError, onCompleted);
    }

    /**
     * Shortcut for {@link #start(String, Single, OnNext, OnError, OnCompleted)} method but with no {@link OnCompleted} action.<br />
     *
     * @param tag      Single tag.
     * @param single   Single to start.
     * @param onNext   OnNext action to call
     * @param onError  OnError action to call
     * @param <Result> Result type of the single.
     */
    public <Result> void start(@NonNull final String tag, @NonNull Single<Result> single, @Nullable final OnNext<V, Result> onNext,
            @Nullable final OnError<V> onError) {
        start(tag, single, onNext, onError, null);
    }

    /**
     * <p>
     * Starts a completable.
     * </p>
     * <p>
     * If an existing completable with the same tag exists in cache, the completable will be resumed.
     * Otherwise it will be added in the cached and started.
     * </p>
     * <p>
     * The withDefaultSchedulers parameter is used to attach or not the completable to default schedulers ({@link
     * RxUtils#applyCompletableIOScheduler}).
     * </p>
     *
     * @param tag                   Completable tag.
     * @param completable           Completable to start.
     * @param withDefaultSchedulers True if default schedulers should be applied.
     * @param onError               OnError action to call
     * @param onCompleted           OnCompleted action to call
     * @param <Result>              Result type of the completable.
     */
    public <Result> void start(@NonNull final String tag, @NonNull Completable completable, boolean withDefaultSchedulers,
            @Nullable final OnError<V> onError, @Nullable final OnCompleted<V> onCompleted) {

        // noinspection unchecked
        CacheableStream<V, Object> cached = (CacheableStream<V, Object>) mCache.get(tag);

        if (!mCache.containsKey(tag)) {
            mCache.put(tag, null);

            MVPLogger.d(mTag, String.format("Starting task : %s", tag));
            if (withDefaultSchedulers) {
                completable = completable.compose(RxUtils.<Result>applyCompletableIOScheduler());
            }
            cached = new CacheableStream<>(
                    completable,
                    mView,
                    getCacheableOnTerminateAction(tag),
                    getCacheableStreamConsumer(null, onError, onCompleted));

            mCache.put(tag, cached);
        } else {
            MVPLogger.d(mTag, String.format("Resuming task : %s", tag));
        }

        if (cached != null) {
            cached.resume();
        }
    }

    /**
     * <p>
     * Shortcut for {@link #start(String, Completable, boolean, OnError, OnCompleted)} method but with no {@link OnCompleted}
     * action.
     * </p>
     * <p>
     * Observable are attached with default schedulers ({@link RxUtils#applyCompletableIOScheduler()}).
     * </p>
     *
     * @param tag         Completable tag.
     * @param completable Completable to start.
     * @param onError     OnError action to call
     * @param onCompleted OnCompleted action to call
     */
    public <Result> void start(@NonNull final String tag, @NonNull Completable completable, @Nullable final OnError<V> onError,
            @Nullable final OnCompleted<V> onCompleted) {
        start(tag, completable, true, onError, onCompleted);
    }

    /**
     * <p>
     * Starts a maybe.
     * </p>
     * <p>
     * If an existing maybe with the same tag exists in cache, the maybe will be resumed.
     * Otherwise it will be added in the cache and started.
     * </p>
     * <p>
     * The withDefaultSchedulers parameter is used to attach or not the maybe to default schedulers ({@link
     * RxUtils#applyMaybeIOScheduler}).
     * </p>
     *
     * @param tag                   Maybe tag.
     * @param maybe                 Maybe to start.
     * @param withDefaultSchedulers True if default schedulers should be applied.
     * @param onNext                OnNext action to call
     * @param onError               OnError action to call
     * @param onCompleted           OnCompleted action to call
     * @param <Result>              Result type of the maybe.
     */
    public <Result> void start(@NonNull final String tag, @NonNull Maybe<Result> maybe, boolean withDefaultSchedulers,
            @Nullable final OnNext<V, Result> onNext, @Nullable final OnError<V> onError, @Nullable final OnCompleted<V> onCompleted) {

        // noinspection unchecked
        CacheableStream<V, Result> cached = (CacheableStream<V, Result>) mCache.get(tag);

        if (!mCache.containsKey(tag)) {
            mCache.put(tag, null);

            MVPLogger.d(mTag, String.format("Starting task : %s", tag));
            if (withDefaultSchedulers) {
                maybe = maybe.compose(RxUtils.<Result>applyMaybeIOScheduler());
            }
            cached = new CacheableStream<>(
                    maybe,
                    mView,
                    getCacheableOnTerminateAction(tag),
                    getCacheableStreamConsumer(onNext, onError, onCompleted));

            mCache.put(tag, cached);
        } else {
            MVPLogger.d(mTag, String.format("Resuming task : %s", tag));
        }

        if (cached != null) {
            cached.resume();
        }
    }

    /**
     * <p>
     * Shortcut for {@link #start(String, Maybe, boolean, OnNext, OnError, OnCompleted)} method but with no {@link OnCompleted}
     * action.
     * </p>
     * <p>
     * Maybe are attached with default schedulers ({@link RxUtils#applyMaybeIOScheduler()}).
     * </p>
     *
     * @param tag         Maybe tag.
     * @param maybe       Maybe to start.
     * @param onNext      OnNext action to call
     * @param onError     OnError action to call
     * @param onCompleted OnCompleted action to call
     * @param <Result>    Result type of the maybe.
     */
    public <Result> void start(@NonNull final String tag, @NonNull Maybe<Result> maybe, @Nullable final OnNext<V, Result> onNext,
            @Nullable final OnError<V> onError, @Nullable final OnCompleted<V> onCompleted) {
        start(tag, maybe, true, onNext, onError, onCompleted);
    }

    /**
     * Shortcut for {@link #start(String, Maybe, OnNext, OnError, OnCompleted)} method but with no {@link OnCompleted} action.<br />
     *
     * @param tag      Maybe tag.
     * @param maybe    Maybe to start.
     * @param onNext   OnNext action to call
     * @param onError  OnError action to call
     * @param <Result> Result type of the maybe.
     */
    public <Result> void start(@NonNull final String tag, @NonNull Maybe<Result> maybe, @Nullable final OnNext<V, Result> onNext,
            @Nullable final OnError<V> onError) {
        start(tag, maybe, onNext, onError, null);
    }
}
