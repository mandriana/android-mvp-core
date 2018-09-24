package com.mandria.android.mvp.kotlin.example

import android.app.Application
import com.mandria.android.mvp.MVPLogger
import com.mandria.android.mvp.kotlin.example.di.AppComponent
import com.mandria.android.mvp.kotlin.example.di.DaggerAppComponent

class MVPApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        MVPLogger.SHOW_MVP_LOGS = true

        appComponent = DaggerAppComponent.builder().application(this).build()
    }

    companion object {
        lateinit var appComponent: AppComponent
    }
}