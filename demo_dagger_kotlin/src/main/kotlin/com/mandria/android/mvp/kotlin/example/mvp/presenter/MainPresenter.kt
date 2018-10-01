package com.mandria.android.mvp.kotlin.example.mvp.presenter

import android.util.Log
import com.mandria.android.mvp.kotlin.example.managers.TaskManager
import com.mandria.android.mvp.kotlin.example.mvp.view.MainView
import com.mandria.android.mvp.rx.RxPresenter
import javax.inject.Inject


class MainPresenter @Inject constructor(
    private val taskManager: TaskManager
) : RxPresenter<MainView>() {

    fun doStuff() {
        start(
            TASK_DO_STUFF, taskManager.longMaybe(),
            { mainView: MainView, s: String ->
                Log.d(TAG, "Task emitted : $s")
                mainView.onTaskSuccess(s)
            },
            { mainView: MainView, throwable: Throwable ->
                Log.e(TAG, "Task failed", throwable)
                mainView.onTaskFailed()
            },
            {
                Log.d(TAG, "Task completed")
                it.onTaskComplete()
            })
    }

    companion object {
        private const val TAG = "MainPresenter"
        private const val TASK_DO_STUFF = "doStuff"
    }
}