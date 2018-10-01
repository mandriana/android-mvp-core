package com.mandria.android.mvp.example.di.modules;

import com.mandria.android.mvp.example.managers.TaskManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class AppModule {

    @Singleton
    @Provides
    public TaskManager getTaskManager() {
        return new TaskManager();
    }
}
