package com.mandria.android.mvp.example.di.modules;

import com.mandria.android.mvp.example.managers.TaskManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by michael on 19/04/2017.
 */
@Module
public class AppModule {

    @Singleton
    @Provides
    public TaskManager getTaskManager() {
        return new TaskManager();
    }
}
