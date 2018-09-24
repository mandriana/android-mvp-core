package com.mandria.android.mvp.kotlin.example.managers

import io.reactivex.*
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

class TaskManager {

    private val DELAY: Long = 5

    fun longObservableTask(): Observable<String> {
        return Observable.fromCallable { "Task completed" }.delay(DELAY, TimeUnit.SECONDS)
    }

    fun longFlowableTask(): Flowable<String> {
        return Flowable.create(FlowableOnSubscribe<String> { e ->
            for (i in 0..9) {
                e.onNext(String.format("emitting %s", i))
            }
            e.onComplete()
        }, BackpressureStrategy.BUFFER)
            .zipWith(Flowable.interval(1000, TimeUnit.MILLISECONDS),
                BiFunction<String, Long, String> { s, _ -> s })
    }

    fun longSingle(): Single<String> {
        return Single.create(SingleOnSubscribe<String> { e -> e.onSuccess("emitting single success") })
            .zipWith(Single.timer(3000, TimeUnit.MILLISECONDS),
                BiFunction<String, Long, String> { s, _ -> s })
    }

    fun longCompletable(): Completable {
        return Completable.create { e -> e.onComplete() }.delay(3000, TimeUnit.MILLISECONDS)
    }

    fun longMaybe(): Maybe<String> {
        return Maybe.create(MaybeOnSubscribe<String> { e -> e.onSuccess("emitting maybe success") })
            .zipWith(Maybe.timer(3000, TimeUnit.MILLISECONDS),
                BiFunction<String, Long, String> { s, _ -> s })
    }
}