package com.mandria.android.mvp.example.di.modules;

import com.mandria.android.mvp.Presenter;
import com.mandria.android.mvp.example.di.annotations.PresenterKey;
import com.mandria.android.mvp.example.mvp.presenters.UserPresenter;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class UserPresenterModule {

    @Binds
    @IntoMap
    @PresenterKey(UserPresenter.class)
    abstract Presenter bindUserPresenter(UserPresenter presenter);
}
