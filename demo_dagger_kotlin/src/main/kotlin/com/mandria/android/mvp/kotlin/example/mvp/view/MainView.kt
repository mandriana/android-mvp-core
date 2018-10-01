package com.mandria.android.mvp.kotlin.example.mvp.view


interface MainView {
    fun onTaskSuccess(result: String)

    fun onTaskComplete()

    fun onTaskFailed()

}