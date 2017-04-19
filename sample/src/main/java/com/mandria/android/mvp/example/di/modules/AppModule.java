package com.mandria.android.mvp.example.di.modules;

import com.mandria.android.mvp.example.MVPApplication;
import com.mandria.android.mvp.example.managers.TaskManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by michael on 19/04/2017.
 */
@Module
public class AppModule {

    private final MVPApplication mApplicationContext;


    public AppModule(MVPApplication applicationContext) {
        mApplicationContext = applicationContext;
    }

    @Provides
    @Singleton
    public MVPApplication getApplicationContext(){
        return mApplicationContext;
    }

    @Provides
    @Singleton
    public TaskManager getTaskManager(){
        return new TaskManager();
    }

}
