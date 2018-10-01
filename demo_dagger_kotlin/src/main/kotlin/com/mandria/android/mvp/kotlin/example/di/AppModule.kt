package com.mandria.android.mvp.kotlin.example.di

import com.mandria.android.mvp.kotlin.example.managers.TaskManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Singleton
    @Provides
    fun getTaskManager(): TaskManager = TaskManager()
}