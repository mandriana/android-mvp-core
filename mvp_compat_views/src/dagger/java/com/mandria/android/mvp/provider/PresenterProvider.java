package com.mandria.android.mvp.provider;

import com.mandria.android.mvp.HasPresenter;
import com.mandria.android.mvp.Presenter;
import com.mandria.android.mvp.PresenterCache;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

/**
 * This class is responsible of the presenter lifecycle and presenter provision.
 */
public final class PresenterProvider {

    public static final String CONTROLLER_STATE_KEY = "com.mandria.android.mvp.presenter.controller.state";

    private static final String PRESENTER_BUNDLE_KEY = "com.mandria.android.mvp.presenter.bundle";

    private static final String PRESENTER_ID_KEY = "com.mandria.android.mvp.presenter.id";

    private final PresenterCache mPresenterCache;

    private final PresenterFactory mPresenterFactory;

    private Bundle mBundle;

    private Presenter mPresenter;

    private boolean mPresenterHasView;

    @Inject
    public PresenterProvider(PresenterCache presenterCache, PresenterFactory presenterFactory) {
        mPresenterCache = presenterCache;
        mPresenterFactory = presenterFactory;
    }

    /**
     * Loads the presenter from cache if available.
     *
     * @return True if presenter was retrieved from cache, else false.
     */
    private boolean loadPresenterFromCache() {
        // Try to retrieve presenter from cache
        // Bundle should not be null if presenter is already in cache
        if (mPresenter == null && mBundle != null) {
            mPresenter = mPresenterCache.getPresenter(mBundle.getString(PRESENTER_ID_KEY));
        }

        return mPresenter != null;
    }

    /**
     * Creates the presenter.
     */
    private <P extends Presenter> void createPresenter(@NonNull Class<P> presenterClass) {
        if (mPresenter == null) {
            mPresenter = mPresenterFactory.create(presenterClass);
            mPresenterCache.savePresenter(mPresenter);
            // Passes presenter bundle if available
            mPresenter.create(mBundle == null ? null : mBundle.getBundle(PRESENTER_BUNDLE_KEY));
        }

        mBundle = null;
    }

    /**
     * Prepares the presenter by retrieving it from the cache or instantiating a new one using the factory.
     *
     * @param presenterOwner Presenter owner that implements {@link HasPresenter}.
     * @param <P>            Presenter.
     */
    public <P extends Presenter> void preparePresenter(@NonNull HasPresenter<P> presenterOwner) {
        PresenterClass annotation = presenterOwner.getClass().getAnnotation(PresenterClass.class);
        if (annotation == null) {
            throw new IllegalArgumentException("HasPresenter owner does not provide @PresenterClass annotation");
        }
        if (!loadPresenterFromCache()) {
            createPresenter(annotation.value());
        }
    }

    /**
     * Allows the presenter to save its state.
     *
     * @return A bundle with presenter state.
     */
    public Bundle onSaveInstanceState() {
        Bundle controllerBundle = new Bundle();

        // Stores the presenter bundle in this controller bundle
        Bundle presenterBundle = new Bundle();
        mPresenter.save(presenterBundle);
        controllerBundle.putBundle(PRESENTER_BUNDLE_KEY, presenterBundle);

        // Saves the presenter id in the bundle to reattach the view to the presenter
        controllerBundle.putString(PRESENTER_ID_KEY, mPresenterCache.getId(mPresenter));

        return controllerBundle;
    }

    /**
     * Allows the presenter to restore its state.
     *
     * @param presenterState Bundle with the presenter state.
     */
    public void onRestoreInstanceState(@Nullable Bundle presenterState) {
        mBundle = presenterState;
    }

    /**
     * Attaches the view to the presenter.
     *
     * @param view View to attache.
     */
    @SuppressWarnings("unchecked")
    public void attachViewToPresenter(@NonNull Object view) {
        if (!mPresenterHasView) {
            if (mPresenter.getView() == null) {
                mPresenter.attachView(view);
                if (!mPresenterHasView) {
                    mPresenter.onCreatedThenAttached();
                }
                mPresenterHasView = true;
            }
        }
    }

    /**
     * Detaches the view from the presenter and destroys it if asked.
     *
     * @param destroy True if the {@link Presenter#destroy()} should be called..
     */
    public void detachViewFromPresenter(boolean destroy) {
        // Presenter can be null if doing on back :
        // - onPause is called
        // - finish is called
        if (mPresenter != null) {
            if (mPresenterHasView) {
                mPresenter.detachView();
                mPresenterHasView = false;
            }
            if (destroy) {
                mPresenter.destroy();
                mPresenterCache.removePresenter(mPresenter);
                mPresenter = null;
            }
        }
    }

    /**
     * Calls the {@link Presenter#destroy()} method.
     */
    public void destroy() {
        detachViewFromPresenter(true);
    }

    /**
     * @return Presenter instance.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public <P extends Presenter> P getPresenter() {
        if (mPresenter == null) {
            throw new IllegalStateException("Call preparePresenter() before accessing presenter");
        }
        return (P) mPresenter;
    }
}
