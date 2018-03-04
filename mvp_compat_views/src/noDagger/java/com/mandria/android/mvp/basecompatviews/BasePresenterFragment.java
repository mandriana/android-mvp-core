package com.mandria.android.mvp.basecompatviews;

import com.mandria.android.mvp.HasPresenter;
import com.mandria.android.mvp.Presenter;
import com.mandria.android.mvp.PresenterCache;
import com.mandria.android.mvp.PresenterController;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Base fragment for fragment which should use a presenter.
 */
public abstract class BasePresenterFragment<P extends Presenter> extends Fragment implements HasPresenter<P> {

    /**
     * A controller class to handle lifecycle with presenters.
     */
    private PresenterController<P> mPresenterController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenterController = new PresenterController<P>(getPresenterCache()) {
            @NonNull
            @Override
            public P instantiatePresenter() {
                return BasePresenterFragment.this.instantiatePresenter();
            }
        };

        if (savedInstanceState != null) {
            mPresenterController
                    .onRestoreInstanceState(savedInstanceState.getBundle(PresenterController.CONTROLLER_STATE_KEY));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(PresenterController.CONTROLLER_STATE_KEY, mPresenterController.onSaveInstanceState());
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenterController.attachViewToPresenter(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenterController.detachViewFromPresenter(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenterController.detachViewFromPresenter(!getActivity().isChangingConfigurations());
    }

    @Override
    public P getPresenter() {
        return mPresenterController.getPresenter();
    }

    /**
     * Forces the destruction of a presenter :
     * - presenter will be notified of their destruction with {@link Presenter#detachView()} first then {@link Presenter#destroy()}.
     * - presenter will be removed from the cache
     */
    public void destroyPresenter() {
        mPresenterController.destroy();
    }

    /**
     * This method should return a Presenter instance which will be used for the current view.
     *
     * @return A presenter instance.
     */
    @NonNull
    protected abstract P instantiatePresenter();

    /**
     * Gets the presenter cache.
     *
     * @return Presenter cache.
     */
    @NonNull
    protected abstract PresenterCache getPresenterCache();
}
