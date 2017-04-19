package com.mandria.android.mvp.example.managers;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * Created by michael on 19/04/2017.
 */
public class TaskManager {

    private static final long DELAY = 5;

    public Observable<String> longTask() {
        return Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "Task completed";
            }
        }).delay(DELAY, TimeUnit.SECONDS);
    }
}
