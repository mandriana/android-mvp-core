package com.mandria.android.mvp.example.di.modules;

import com.mandria.android.mvp.Presenter;
import com.mandria.android.mvp.example.di.annotations.PresenterKey;
import com.mandria.android.mvp.example.mvp.presenters.MainPresenter;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class PresenterModule {

    @Binds
    @IntoMap
    @PresenterKey(MainPresenter.class)
    abstract Presenter bindMainPresenter(MainPresenter presenter);
}
