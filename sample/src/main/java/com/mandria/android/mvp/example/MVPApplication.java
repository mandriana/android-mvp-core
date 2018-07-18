package com.mandria.android.mvp.example;

import com.mandria.android.mvp.MVPLogger;
import com.mandria.android.mvp.example.di.components.AppComponent;
import com.mandria.android.mvp.example.di.components.DaggerAppComponent;
import com.mandria.android.mvp.example.di.components.UserComponent;

import android.app.Activity;
import android.app.Application;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

/**
 * Created by michael on 19/04/2017.
 */
public class MVPApplication extends Application implements HasActivityInjector {

    private static AppComponent mAppComponent;

    private static UserComponent mUserComponent;

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

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

        mAppComponent.inject(this);
    }

    public static void makeUserComponent() {
        mUserComponent = mAppComponent.createUserComponent().build();
    }

    public static UserComponent getUserComponent() {
        return mUserComponent;
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }
}
