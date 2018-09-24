package com.mandria.android.mvp.di

import com.mandria.android.mvp.PresenterCache
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/** Dagger module which will provide core application scoped objects. **/
@Module
class CoreModule {

    /** Provides the presenter cache. **/
    @Provides
    @Singleton
    fun getPresenterCache(): PresenterCache = PresenterCache()
}