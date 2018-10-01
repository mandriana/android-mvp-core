package com.mandria.android.mvp.example.mvp.presenters;

import android.util.Log;

import com.mandria.android.mvp.example.managers.TaskManager;
import com.mandria.android.mvp.example.mvp.views.MainView;
import com.mandria.android.mvp.rx.RxPresenter;
import com.mandria.android.mvp.rx.callbacks.OnCompleted;
import com.mandria.android.mvp.rx.callbacks.OnError;
import com.mandria.android.mvp.rx.callbacks.OnNext;

import javax.inject.Inject;

import androidx.annotation.NonNull;


public class MainPresenter extends RxPresenter<MainView> {

    private static final String TAG = "MainPresenter";

    private static final String TASK_DO_STUFF = "doStuff";

    private final TaskManager mTaskManager;

    @Inject
    public MainPresenter(TaskManager taskManager) {
        mTaskManager = taskManager;
    }

    public void doStuff() {
        start(TASK_DO_STUFF, mTaskManager.longFlowableTask(), true,
                new OnNext<MainView, String>() {
                    @Override
                    public void accept(MainView mainView, String s) throws Exception {
                        Log.d(TAG, "Task emitted : " + s);
                        mainView.onTaskSuccess(s);
                    }
                },
                new OnError<MainView>() {
                    @Override
                    public void accept(@NonNull MainView mainView, @NonNull Throwable throwable) {
                        Log.e(TAG, "Task failed", throwable);
                        mainView.onTaskFailed();
                    }
                },
                new OnCompleted<MainView>() {
                    @Override
                    public void accept(@NonNull MainView mainView) {
                        Log.d(TAG, "Task completed");
                        mainView.onTaskComplete();
                    }
                });
    }
}
