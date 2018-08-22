package com.mandria.android.mvp.example;

import com.mandria.android.mvp.MVPLogger;
import com.mandria.android.mvp.example.di.components.AppComponent;
import com.mandria.android.mvp.example.di.components.DaggerAppComponent;
import com.mandria.android.mvp.example.di.components.UserComponent;

import android.app.Application;

/**
 * Created by michael on 19/04/2017.
 */
public class MVPApplication extends Application {

    public static AppComponent mAppComponent;

    public static UserComponent mUserComponent;

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

    public static void makeUserComponent() {
        mUserComponent = mAppComponent.createUserComponent().build();
    }
}
