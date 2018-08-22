package com.mandria.android.mvp.basecompatviews;

import android.os.Bundle;

import com.mandria.android.mvp.HasPresenter;
import com.mandria.android.mvp.Presenter;
import com.mandria.android.mvp.PresenterController;
import com.mandria.android.mvp.provider.PresenterProvider;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Base fragment for fragment which should use a presenter.
 */
public abstract class BasePresenterFragment<P extends Presenter> extends Fragment implements HasPresenter<P> {

    /**
     * A provider class to handle lifecycle with presenters.
     */
    @Inject
    PresenterProvider mPresenterProvider;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mPresenterProvider.onRestoreInstanceState(savedInstanceState.getBundle(PresenterController.CONTROLLER_STATE_KEY));
        }

        mPresenterProvider.preparePresenter(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(PresenterProvider.CONTROLLER_STATE_KEY, mPresenterProvider.onSaveInstanceState());
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenterProvider.attachViewToPresenter(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenterProvider.detachViewFromPresenter(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenterProvider.detachViewFromPresenter(!getActivity().isChangingConfigurations());
    }

    @Override
    public P getPresenter() {
        return mPresenterProvider.getPresenter();
    }

    /**
     * Forces the destruction of a presenter :
     * - presenter will be notified of their destruction with {@link Presenter#detachView()} first then {@link Presenter#destroy()}.
     * - presenter will be removed from the cache
     */
    public void destroyPresenter() {
        mPresenterProvider.destroy();
    }
}
