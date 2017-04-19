package com.mandria.android.mvp;

/**
 * Interface which should be implemented by the view which will contain a presenter.
 */
public interface HasPresenter<P extends Presenter> {

    /**
     * Gets the presenter.
     *
     * @return Presenter.
     */
    P getPresenter();
}
