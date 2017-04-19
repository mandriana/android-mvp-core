package com.mandria.android.mvp.rx;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A RxJava utils class.
 */
class RxUtils {

    private static final Observable.Transformer<Observable, Observable> IOSchedulerTransformer
            = new Observable.Transformer<Observable, Observable>() {
        @Override
        public Observable<Observable> call(Observable<Observable> observable) {
            return observable.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
        }
    };

    /**
     * Constructor.
     */
    private RxUtils() {
        // unused
    }

    /**
     * Apply the {@link AndroidSchedulers#mainThread()} scheduler to observe on the observable and the {@link Schedulers#io()}
     * to subscribe on the observale.
     *
     * @param <T> Observable type.
     * @return An observable of the same type with the applied schedulers.
     */
    @SuppressWarnings("unchecked")
    static <T> Observable.Transformer<T, T> applyIOScheduler() {
        return (Observable.Transformer<T, T>) IOSchedulerTransformer;
    }
}
