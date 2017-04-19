package com.mandria.android.mvp.di;

import com.mandria.android.mvp.PresenterCache;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module which will provide core application scoped objects.
 */
@Module
public class CoreModule {

    /**
     * Constructor.
     */
    public CoreModule() {

    }

    /**
     * Provides the presenter cache.
     *
     * @return Presenter cache.
     */
    @Provides
    @Singleton
    public PresenterCache getPresenterCache() {
        return new PresenterCache();
    }
}
