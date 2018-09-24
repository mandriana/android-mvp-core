package com.mandria.android.mvp.kotlin.example.mvp.presenter

import android.util.Log
import com.mandria.android.mvp.kotlin.example.mvp.view.ListDatabaseView
import com.mandria.android.mvp.rx.RxPresenter
import com.mandria.android.mvp.rx.callbacks.OnError
import com.mandria.android.mvp.rx.callbacks.OnNext
import io.reactivex.Single
import javax.inject.Inject


class ListDatabasePresenter @Inject constructor() : RxPresenter<ListDatabaseView>() {

    fun insertDatabase() {
        Log.i("ListDatabasePresenter", "STARTING -------------")
        start("test", Single.just("okay"),
            object : OnNext<ListDatabaseView, String> {
                override fun accept(u: ListDatabaseView, v: String) {
                    Log.i("ListDatabasePresenter", "DONE -------------")
                }

            },
            object : OnError<ListDatabaseView> {
                override fun accept(u: ListDatabaseView, throwable: Throwable) {
                    Log.i("ListDatabasePresenter", "ERROR -------------")
                }
            })
    }
}