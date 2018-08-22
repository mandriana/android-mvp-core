package com.mandria.android.mvp.rx;

import org.reactivestreams.Publisher;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.CompletableTransformer;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.MaybeTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * A RxJava utils class.
 */
class RxUtils {

    private static final ObservableTransformer<Observable, Observable> observableIOSchedulerTransformer
            = new ObservableTransformer<Observable, Observable>() {
        @Override
        public Observable<Observable> apply(Observable<Observable> observable) {
            return observable.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
        }
    };

    private static final FlowableTransformer<Flowable, Flowable> flowableIOSchedulerTransformer
            = new FlowableTransformer<Flowable, Flowable>() {
        @Override
        public Publisher<Flowable> apply(@NonNull Flowable<Flowable> upstream) {
            return upstream.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
        }
    };

    private static final SingleTransformer<Single, Single> singleIOSchedulerTransformer
            = new SingleTransformer<Single, Single>() {
        @Override
        public SingleSource<Single> apply(@NonNull Single<Single> upstream) {
            return upstream.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
        }
    };

    private static final CompletableTransformer completableIOSchedulerTransformer
            = new CompletableTransformer() {
        @Override
        public CompletableSource apply(@NonNull Completable upstream) {
            return upstream.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
        }
    };

    private static final MaybeTransformer<Maybe, Maybe> maybeIOSchedulerTransformer
            = new MaybeTransformer<Maybe, Maybe>() {
        @Override
        public MaybeSource<Maybe> apply(@NonNull Maybe<Maybe> upstream) {
            return upstream.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
        }
    };

    /**
     * Constructor.
     */
    private RxUtils() {
        // unuseda
    }

    /**
     * Apply the {@link AndroidSchedulers#mainThread()} scheduler to observe on the observable and the {@link Schedulers#io()}
     * to subscribe on the observable.
     *
     * @param <T> Observable type.
     * @return An observable of the same type with the applied schedulers.
     */
    @SuppressWarnings("unchecked")
    static <T> ObservableTransformer<T, T> applyObservableIOScheduler() {
        return (ObservableTransformer<T, T>) observableIOSchedulerTransformer;
    }

    /**
     * Apply the {@link AndroidSchedulers#mainThread()} scheduler to observe on the flowable and the {@link Schedulers#io()}
     * to subscribe on the flowable.
     *
     * @param <T> Observable type.
     * @return An observable of the same type with the applied schedulers.
     */
    @SuppressWarnings("unchecked")
    static <T> FlowableTransformer<T, T> applyFlowableIOScheduler() {
        return (FlowableTransformer<T, T>) flowableIOSchedulerTransformer;
    }

    /**
     * Apply the {@link AndroidSchedulers#mainThread()} scheduler to observe on the single stream and the {@link Schedulers#io()}
     * to subscribe on the single stream.
     *
     * @param <T> Observable type.
     * @return An observable of the same type with the applied schedulers.
     */
    @SuppressWarnings("unchecked")
    static <T> SingleTransformer<T, T> applySingleIOScheduler() {
        return (SingleTransformer<T, T>) singleIOSchedulerTransformer;
    }

    /**
     * Apply the {@link AndroidSchedulers#mainThread()} scheduler to observe on the completable stream and the {@link Schedulers#io()}
     * to subscribe on the completable stream.
     *
     * @return An observable of the same type with the applied schedulers.
     */
    @SuppressWarnings("unchecked")
    static CompletableTransformer applyCompletableIOScheduler() {
        return completableIOSchedulerTransformer;
    }

    /**
     * Apply the {@link AndroidSchedulers#mainThread()} scheduler to observe on the maybe stream and the {@link Schedulers#io()}
     * to subscribe on the maybe stream.
     *
     * @param <T> Observable type.
     * @return An observable of the same type with the applied schedulers.
     */
    @SuppressWarnings("unchecked")
    static <T> MaybeTransformer<T, T> applyMaybeIOScheduler() {
        return (MaybeTransformer<T, T>) maybeIOSchedulerTransformer;
    }
}
