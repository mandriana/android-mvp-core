package com.mandria.android.mvp.rx

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

private val observableIOSchedulerTransformer =
    ObservableTransformer<Observable<*>, Observable<*>> { observable ->
        observable.observeOn(
            AndroidSchedulers.mainThread()
        ).subscribeOn(Schedulers.io())
    }

private val flowableIOSchedulerTransformer =
    FlowableTransformer<Flowable<*>, Flowable<*>> { upstream ->
        upstream.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

private val singleIOSchedulerTransformer =
    SingleTransformer<Single<*>, Single<*>> { upstream ->
        upstream.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

private val completableIOSchedulerTransformer = CompletableTransformer { upstream ->
    upstream.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
}

private val maybeIOSchedulerTransformer =
    MaybeTransformer<Maybe<*>, Maybe<*>> { upstream ->
        upstream.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

/**
 * Apply the [AndroidSchedulers.mainThread] scheduler to observe on the observable and the [Schedulers.io]
 * to subscribe on the observable.
 *
 * @param <T> Observable type.
 * @return An observable of the same type with the applied schedulers.
 */
@Suppress("UNCHECKED_CAST")
fun <T> applyObservableIOScheduler(): ObservableTransformer<T, T> {
    return observableIOSchedulerTransformer as ObservableTransformer<T, T>
}

/**
 * Apply the [AndroidSchedulers.mainThread] scheduler to observe on the flowable and the [Schedulers.io]
 * to subscribe on the flowable.
 *
 * @param <T> Observable type.
 * @return An observable of the same type with the applied schedulers.
 */
@Suppress("UNCHECKED_CAST")
fun <T> applyFlowableIOScheduler(): FlowableTransformer<T, T> {
    return flowableIOSchedulerTransformer as FlowableTransformer<T, T>
}

/**
 * Apply the [AndroidSchedulers.mainThread] scheduler to observe on the single stream and the [Schedulers.io]
 * to subscribe on the single stream.
 *
 * @param <T> Observable type.
 * @return An observable of the same type with the applied schedulers.
 */
@Suppress("UNCHECKED_CAST")
fun <T> applySingleIOScheduler(): SingleTransformer<T, T> {
    return singleIOSchedulerTransformer as SingleTransformer<T, T>
}

/**
 * Apply the [AndroidSchedulers.mainThread] scheduler to observe on the completable stream and the [Schedulers.io]
 * to subscribe on the completable stream.
 *
 * @return An observable of the same type with the applied schedulers.
 */
@Suppress("UNCHECKED_CAST")
fun applyCompletableIOScheduler(): CompletableTransformer {
    return completableIOSchedulerTransformer
}

/**
 * Apply the [AndroidSchedulers.mainThread] scheduler to observe on the maybe stream and the [Schedulers.io]
 * to subscribe on the maybe stream.
 *
 * @param <T> Observable type.
 * @return An observable of the same type with the applied schedulers.
 */
@Suppress("UNCHECKED_CAST")
fun <T> applyMaybeIOScheduler(): MaybeTransformer<T, T> {
    return maybeIOSchedulerTransformer as MaybeTransformer<T, T>
}