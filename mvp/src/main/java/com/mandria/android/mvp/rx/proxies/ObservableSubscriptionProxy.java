package com.mandria.android.mvp.rx.proxies;

import com.mandria.android.mvp.rx.BoundData;
import com.mandria.android.mvp.rx.RxView;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.ReplaySubject;

/**
 * Proxy for the original observable subscription.
 * A {@link ReplaySubject} subscribes to the observable and is used to attach the {@link Consumer}.
 */
public class ObservableSubscriptionProxy<View, Result> extends AbstractSubscriptionProxy<View, Result> {

    private final String mTag = getClass().getSimpleName();

    private final DisposableObserver<Result> mReplayDisposable;

    private Observable<BoundData<View, Result>> mObservable;

    /**
     * Constructor.
     *
     * @param observable  Original observable.
     * @param view        Observable that emits the view.
     */
    public ObservableSubscriptionProxy(Observable<Result> observable, Observable<RxView<View>> view) {
        super();

        // Creates a replay subject which will subscribe to the observable.
        final ReplaySubject<Result> replaySubject = ReplaySubject.create();

        mReplayDisposable = new DisposableObserver<Result>() {
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
        observable.subscribe(mReplayDisposable);

        // Keeps as the observable reference the combination of the view behaviour subject and the replay subject
        // so that the original observable can continue its work and we can dispose from the replay subject
        mObservable = Observable
                .combineLatest(
                        view,
                        replaySubject.materialize(),
                        mCombineFunction)
                .filter(mFilterPredicate);

        // Adds the replaySubject subscription to the CompositeSubscription
        // to be able to dispose the replaySubject from the original observable
        mCompositeDisposable.add(mReplayDisposable);
    }

    @Override
    public Disposable subscribe(Consumer<BoundData<View, Result>> consumer) {
        dispose();
        mDisposable = mObservable.subscribe(consumer);
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
