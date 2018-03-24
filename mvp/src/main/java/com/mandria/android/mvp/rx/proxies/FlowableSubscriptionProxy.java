package com.mandria.android.mvp.rx.proxies;

import com.mandria.android.mvp.MVPLogger;
import com.mandria.android.mvp.rx.BoundData;
import com.mandria.android.mvp.rx.RxView;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.ReplayProcessor;
import io.reactivex.subscribers.DisposableSubscriber;

/**
 * Proxy for the original observable subscription.
 * A {@link ReplayProcessor} subscribes to the flowable and is used to attach the {@link Consumer}.
 */
public class FlowableSubscriptionProxy<View, Result> extends AbstractSubscriptionProxy<View, Result> {

    private final String mTag = getClass().getSimpleName();

    private final DisposableSubscriber<Result> mReplayDisposable;

    private Flowable<BoundData<View, Result>> mFlowable;

    /**
     * Constructor.
     *
     * @param flowable    Original flowable.
     * @param view        Observable that emits the view.
     * @param onTerminate Action to perform when the ReplaySubject will terminate.
     */
    public FlowableSubscriptionProxy(Flowable<Result> flowable, Observable<RxView<View>> view, final Action onTerminate) {
        super(onTerminate);

        // Creates a replay subject which will subscribe to the flowable.
        final ReplayProcessor<Result> replaySubject = ReplayProcessor.create();

        mReplayDisposable = new DisposableSubscriber<Result>() {
            @Override
            public void onNext(@NonNull Result result) {
                replaySubject.onNext(result);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                replaySubject.onError(e);
            }

            @Override
            public void onComplete() {
                replaySubject.onComplete();
            }
        };
        flowable.subscribe(mReplayDisposable);

        // Keeps as the flowable reference the combination of the view behaviour subject and the replay processor
        // so that the original flowable can continue its work and we can dispose from the replay process
        // View is converted to flowable with BackpressureStrategy.LATEST, since only the latest emission interests us
        mFlowable = Flowable
                .combineLatest(
                        view.toFlowable(BackpressureStrategy.LATEST),
                        replaySubject.materialize(),
                        mCombineFunction
                )
                .filter(mFilterPredicate)
                .doAfterNext(new Consumer<BoundData<View, Result>>() {
                    @Override
                    public void accept(BoundData<View, Result> viewResultBoundData) throws Exception {
                        Notification<Result> notification = viewResultBoundData.getData();
                        if (notification.isOnComplete() || notification.isOnError()) {
                            try {
                                mOnTerminate.run();
                            } catch (Exception e) {
                                MVPLogger.e(mTag, e.getMessage());
                            }
                        }
                    }
                });

        // Adds the replaySubject subscription to the CompositeSubscription
        // to be able to dispose the replaySubject from the original flowable
        mCompositeDisposable.add(mReplayDisposable);
    }

    @Override
    public Disposable subscribe(Consumer<BoundData<View, Result>> consumer) {
        dispose();
        mDisposable = mFlowable.subscribe(consumer);
        mCompositeDisposable.add(mDisposable);
        return mDisposable;
    }

    @Override
    public void dispose() {
        if (mDisposable != null) {
            mCompositeDisposable.remove(mDisposable);
        }
    }

    /**
     * Gets if the observable is canceled.
     *
     * @return True if no subscription remains.
     */
    boolean isCanceled() {
        return isDisposed() && mReplayDisposable.isDisposed();
    }
}
