package com.mandria.android.mvp.example;

import android.app.Application;

import com.mandria.android.mvp.MVPLogger;
import com.mandria.android.mvp.example.di.components.AppComponent;
import com.mandria.android.mvp.example.di.components.DaggerAppComponent;


public class MVPApplication extends Application {

    public static AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        MVPLogger.SHOW_MVP_LOGS = true;

        initializeInjector();
    }

    /**
     * Initializes the injector.
     */
    private void initializeInjector() {
        mAppComponent = DaggerAppComponent
                .builder()
                .application(this)
                .build();
    }
}
