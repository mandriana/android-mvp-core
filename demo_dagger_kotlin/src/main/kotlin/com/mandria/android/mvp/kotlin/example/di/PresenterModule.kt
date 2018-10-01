package com.mandria.android.mvp.kotlin.example.di

import com.mandria.android.mvp.Presenter
import com.mandria.android.mvp.kotlin.example.mvp.presenter.MainPresenter
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
abstract class PresenterModule {

    @Binds
    @IntoMap
    @PresenterKey(MainPresenter::class)
    abstract fun bindDatabasePresenter(presenter: MainPresenter): Presenter<*>
}