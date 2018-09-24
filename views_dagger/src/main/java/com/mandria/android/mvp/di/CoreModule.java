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
     * Provides the presenter cache.
     *
     * @return Presenter cache.
     */
    @Singleton
    @Provides
    public PresenterCache getPresenterCache() {
        return new PresenterCache();
    }
}
