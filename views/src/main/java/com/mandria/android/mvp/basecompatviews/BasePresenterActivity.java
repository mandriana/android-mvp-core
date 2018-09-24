package com.mandria.android.mvp.basecompatviews;

import android.os.Bundle;

import com.mandria.android.mvp.HasPresenter;
import com.mandria.android.mvp.Presenter;
import com.mandria.android.mvp.PresenterCache;
import com.mandria.android.mvp.PresenterController;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Base activity for activity which should use a presenter.
 */
public abstract class BasePresenterActivity<P extends Presenter> extends AppCompatActivity implements HasPresenter<P> {

    /**
     * A controller class to handle lifecycle with presenters.
     */
    private PresenterController<P> mPresenterController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenterController = new PresenterController<P>(getPresenterCache()) {
            @NonNull
            @Override
            public P instantiatePresenter() {
                return BasePresenterActivity.this.instantiatePresenter();
            }
        };

        if (savedInstanceState != null) {
            mPresenterController.onRestoreInstanceState(savedInstanceState.getBundle(PresenterController.CONTROLLER_STATE_KEY));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(PresenterController.CONTROLLER_STATE_KEY, mPresenterController.onSaveInstanceState());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenterController.attachViewToPresenter(this);
    }

    @Override
    protected void onPause() {
        mPresenterController.detachViewFromPresenter(isFinishing());
        super.onPause();
    }

    @Override
    public void finish() {
        mPresenterController.detachViewFromPresenter(true);
        super.finish();
    }

    @Override
    public final P getPresenter() {
        return mPresenterController.getPresenter();
    }

    /**
     * * Forces the destruction of a presenter.
     */
    public final void destroyPresenter() {
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
