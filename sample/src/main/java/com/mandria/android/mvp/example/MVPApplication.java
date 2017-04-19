package com.mandria.android.mvp.example;

import com.mandria.android.mvp.di.CoreModule;
import com.mandria.android.mvp.example.di.components.AppComponent;
import com.mandria.android.mvp.example.di.components.DaggerAppComponent;
import com.mandria.android.mvp.example.di.modules.AppModule;

import android.app.Application;

/**
 * Created by michael on 19/04/2017.
 */
public class MVPApplication extends Application {

    private static AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        initializeInjector();
    }

    /**
     * Initializes the injector.
     */
    private void initializeInjector() {
        mAppComponent = DaggerAppComponent.builder()
                .coreModule(new CoreModule())
                .appModule(new AppModule(this))
                .build();
    }

    public static AppComponent getAppComponent() {
        return mAppComponent;
    }
}
