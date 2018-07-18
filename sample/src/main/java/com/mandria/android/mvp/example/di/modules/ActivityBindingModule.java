package com.mandria.android.mvp.example.di.modules;

import com.mandria.android.mvp.example.activities.MainActivity;
import com.mandria.android.mvp.example.activities.UserActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBindingModule {

    @ContributesAndroidInjector
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector
    abstract UserActivity contributeUserActivity();
}
