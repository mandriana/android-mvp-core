package com.mandria.android.mvp.example.managers;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;

/**
 * Created by michael on 19/04/2017.
 */
public class TaskManager {

    private static final long DELAY = 5;

    public Observable<String> longObservableTask() {
        return Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "Task completed";
            }
        }).delay(DELAY, TimeUnit.SECONDS);
    }

    public Flowable<String> longFlowableTask() {
        return Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<String> e) throws Exception {
                for (int i = 0; i < 10; i++) {
                    e.onNext(String.format("emitting %s", i));
                }
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER).zipWith(Flowable.interval(1000, TimeUnit.MILLISECONDS), new BiFunction<String, Long, String>() {
            @Override
            public String apply(@NonNull String s, @NonNull Long aLong) throws Exception {
                return s;
            }
        });
    }

    public Single<String> longSingle() {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<String> e) throws Exception {
                e.onSuccess("emitting single success");
            }
        }).zipWith(Single.timer(3000, TimeUnit.MILLISECONDS), new BiFunction<String, Long, String>() {
            @Override
            public String apply(@NonNull String s, @NonNull Long aLong) throws Exception {
                return s;
            }
        });
    }

    public Completable longCompletable() {
        return Completable.create(new CompletableOnSubscribe() {

            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                e.onComplete();
            }
        }).delay(3000, TimeUnit.MILLISECONDS);
    }

    public Maybe<String> longMaybe() {
        return Maybe.create(new MaybeOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull MaybeEmitter<String> e) throws Exception {
                e.onSuccess("emitting maybe success");
            }
        }).zipWith(Maybe.timer(3000, TimeUnit.MILLISECONDS), new BiFunction<String, Long, String>() {
            @Override
            public String apply(@NonNull String s, @NonNull Long aLong) throws Exception {
                return s;
            }
        });
    }
}
