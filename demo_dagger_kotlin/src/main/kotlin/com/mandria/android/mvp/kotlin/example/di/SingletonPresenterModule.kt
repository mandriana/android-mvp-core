package com.mandria.android.mvp.kotlin.example.di

import com.mandria.android.mvp.Presenter
import com.mandria.android.mvp.kotlin.example.mvp.presenter.ListDatabasePresenter
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
abstract class SingletonPresenterModule {

    @Binds
    @IntoMap
    @PresenterKey(ListDatabasePresenter::class)
    abstract fun bindDatabasePresenter(presenter: ListDatabasePresenter): Presenter<*>
}