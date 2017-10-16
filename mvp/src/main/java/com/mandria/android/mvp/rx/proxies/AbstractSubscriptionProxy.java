package com.mandria.android.mvp.rx.proxies;

import com.mandria.android.mvp.MVPLogger;
import com.mandria.android.mvp.rx.BoundData;
import com.mandria.android.mvp.rx.RxView;

import io.reactivex.Notification;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

/**
 * An abstract class to manipulate subscription proxies.
 */
public abstract class AbstractSubscriptionProxy<View, Result> {

    private final String mTag = getClass().getSimpleName();

    /**
     * Action wrapper to run on original stream termination.
     */
    final Action mOnTerminate;

    /**
     * Combine latest bi-function to apply.
     */
    final BiFunction<RxView<View>, Notification<Result>, BoundData<View, Result>> mCombineFunction;

    /**
     * Predicate to filter item emitted by the combineLatest bi-function.
     */
    final Predicate<BoundData<View, Result>> mFilterPredicate;

    /**
     * Composite disposable to retain replaySubject subscription and combination subscription.
     */
    final CompositeDisposable mCompositeDisposable;

    /**
     * Disposable acquired from the combination subscription.
     */
    Disposable mDisposable;

    /**
     * Constructor.
     *
     * @param onTerminate Termination action to run.
     */
    AbstractSubscriptionProxy(final Action onTerminate) {
        mCompositeDisposable = new CompositeDisposable();

        // Wraps onTerminate action to dispose all disposables
        mOnTerminate = new Action() {
            @Override
            public void run() throws Exception {
                // This method is called after termination consumption
                // we can dispose all
                mCompositeDisposable.dispose();
                try {
                    onTerminate.run();
                } catch (Exception e) {
                    MVPLogger.e(mTag, e.getMessage());
                }
            }
        };

        mCombineFunction = new BiFunction<RxView<View>, Notification<Result>, BoundData<View, Result>>() {
            @Override
            public BoundData<View, Result> apply(@NonNull RxView<View> rxView, @NonNull Notification<Result> replayNotification)
                    throws Exception {
                // In case view is emitted as null
                // we dispose from the replay subject
                // to avoid passing null view (view is detached)
                if (rxView.view == null) {
                    if (mDisposable != null) {
                        dispose();
                    }
                }

                return new BoundData<>(rxView.view, replayNotification);
            }
        };

        // Predicate to filter on view not null
        mFilterPredicate = new Predicate<BoundData<View, Result>>() {
            @Override
            public boolean test(@NonNull BoundData<View, Result> viewResultBoundData) throws Exception {
                return viewResultBoundData.getView() != null;
            }
        };
    }

    /**
     * Cancels the stream disposing all disposables.
     */
    public void cancel() {
        mCompositeDisposable.dispose();
    }

    /**
     * Gets if the disposable attached to the replaySubject is disposed.
     *
     * @return True if unsubscribed.
     */
    boolean isDisposed() {
        return mDisposable != null && mDisposable.isDisposed();
    }

    /**
     * Subscribes to the stream using the given consumer.
     *
     * @param consumer Consumer.
     * @return A disposable to dispose from the stream.
     */
    public abstract Disposable subscribe(Consumer<BoundData<View, Result>> consumer);

    /**
     * Disposes from the stream.
     */
    public abstract void dispose();
}
