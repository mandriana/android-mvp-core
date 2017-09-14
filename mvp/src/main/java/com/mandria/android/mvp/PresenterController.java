package com.mandria.android.mvp;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * A controller to manage presenter lifecycle.
 */
public abstract class PresenterController<P extends Presenter> {

    public static final String CONTROLLER_STATE_KEY = "com.mandria.android.mvp.presenter.controller.state";

    private static final String PRESENTER_BUNDLE_KEY = "com.mandria.android.mvp.presenter.bundle";

    private static final String PRESENTER_ID_KEY = "com.mandria.android.mvp.presenter.id";

    private final PresenterCache mPresenterCache;

    private P mPresenter;

    private Bundle mBundle;

    private boolean mPresenterHasView;

    /**
     * Constructor.
     *
     * @param presenterCache The presenter cache to use.
     */
    public PresenterController(PresenterCache presenterCache) {
        mPresenterCache = presenterCache;
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
    private void createPresenter() {
        if (mPresenter == null) {
            mPresenter = instantiatePresenter();
            mPresenterCache.savePresenter(mPresenter);
            // Passes presenter bundle if available
            mPresenter.create(mBundle == null ? null : mBundle.getBundle(PRESENTER_BUNDLE_KEY));
        }

        mBundle = null;
    }

    /**
     * <p>Gets the presenter from the cache if any or instantiates a new one using {@link #instantiatePresenter()}.<br>
     * Once the presenter is instantiated, this method stores it in the cache and performs the call to {@link Presenter#create(Bundle)}.
     * </p>
     */
    private void createPresenterIfNeeded() {
        if (!loadPresenterFromCache()) {
            createPresenter();
        }
    }

    /**
     * <p>Gets the presenter.<br>
     * Creates the presenter on the first time this method is called.
     * </p>
     *
     * @return The presenter.
     */
    public P getPresenter() {
        createPresenterIfNeeded();
        return mPresenter;
    }

    /**
     * Allows the presenter to save its state.
     *
     * @return A bundle with presenter state.
     */
    public Bundle onSaveInstanceState() {
        createPresenterIfNeeded();

        Bundle controllerBundle = new Bundle();
        if (mPresenter != null) {

            // Stores the presenter bundle in this controller bundle
            Bundle presenterBundle = new Bundle();
            mPresenter.save(presenterBundle);
            controllerBundle.putBundle(PRESENTER_BUNDLE_KEY, presenterBundle);

            // Saves the presenter id in the bundle to reattach the view to the presenter
            controllerBundle.putString(PRESENTER_ID_KEY, mPresenterCache.getId(mPresenter));
        }
        return controllerBundle;
    }

    /**
     * Allows the presenter to restore its state.
     *
     * @param presenterState Bundle with the presenter state.
     */
    public void onRestoreInstanceState(Bundle presenterState) {
        mBundle = presenterState;
    }

    /**
     * Attaches the view to the presenter.
     *
     * @param view View to attache.
     */
    @SuppressWarnings("unchecked")
    public void attachViewToPresenter(Object view) {
        boolean presenterCreated = mPresenter != null;
        getPresenter();
        if (mPresenter != null && !mPresenterHasView) {
            if (mPresenter.getView() == null) {
                mPresenter.attachView(view);
                mPresenterHasView = true;
                if (!presenterCreated) {
                    mPresenter.onCreatedThenAttached();
                }
            }
        }
    }

    /**
     * Detaches the view from the presenter and destroys it if asked.
     *
     * @param destroy True if the {@link Presenter#destroy()} should be called..
     */
    public void detachViewFromPresenter(boolean destroy) {
        if (mPresenter != null && mPresenterHasView) {
            mPresenter.detachView();
            mPresenterHasView = false;
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
     * Instantiates the presenter.
     *
     * @return Presenter.
     */
    @NonNull
    public abstract P instantiatePresenter();
}
