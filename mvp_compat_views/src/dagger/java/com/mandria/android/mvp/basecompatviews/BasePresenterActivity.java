package com.mandria.android.mvp.basecompatviews;

import android.os.Bundle;

import com.mandria.android.mvp.HasPresenter;
import com.mandria.android.mvp.Presenter;
import com.mandria.android.mvp.provider.PresenterProvider;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Base activity for activity which should use a presenter.
 */
public abstract class BasePresenterActivity<P extends Presenter> extends AppCompatActivity implements HasPresenter<P> {

    /**
     * A provider class to handle lifecycle with presenters.
     */
    @Inject
    PresenterProvider mPresenterProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mPresenterProvider.onRestoreInstanceState(savedInstanceState.getBundle(PresenterProvider.CONTROLLER_STATE_KEY));
        }

        mPresenterProvider.preparePresenter(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(PresenterProvider.CONTROLLER_STATE_KEY, mPresenterProvider.onSaveInstanceState());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenterProvider.attachViewToPresenter(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenterProvider.detachViewFromPresenter(isFinishing());
    }

    @Override
    public void finish() {
        mPresenterProvider.detachViewFromPresenter(true);
        super.finish();
    }

    @Override
    public P getPresenter() {
        return mPresenterProvider.getPresenter();
    }

    /**
     * Forces the destruction of a presenter.
     */
    public void destroyPresenter() {
        mPresenterProvider.destroy();
    }
}
