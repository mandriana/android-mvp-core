package com.mandria.android.mvp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Base presenter.
 */
public abstract class Presenter<V> {

    private V mView;

    /**
     * <p>This method is called when the presenter is created.<br>
     * It is not attached to any "onCreate" method from Activity nor Fragment.<br>
     * While the presenter remains in cache, this method would have been called only once.</p>
     *
     * @param savedState The bundle used to save presenter data, may be null.
     */
    protected void onCreate(@Nullable Bundle savedState) {
        MVPLogger.d(getClass().getSimpleName(), "On create presenter");
    }

    /**
     * This method is called after the presenter is created and attached the first time to the view.
     */
    public void onCreatedThenAttached() {
        MVPLogger.d(getClass().getSimpleName(), "On presenter created then view attached");
    }

    /**
     * This method is called when the user leaves the view.
     */
    protected void onDestroy() {
        MVPLogger.d(getClass().getSimpleName(), "On destroy presenter");
    }

    /**
     * A returned state is the state that will be passed to {@link #onCreate} for a new presenter instance after a process restart.
     *
     * @param state Presenter bundle state.
     */
    protected void onSave(@NonNull Bundle state) {
        MVPLogger.d(getClass().getSimpleName(), "On save presenter state");
    }

    /**
     * This method is called when the view is attached to this presenter.
     *
     * @param view The view which is now attached.
     */
    protected void onViewAttached(@NonNull V view) {
        MVPLogger.d(getClass().getSimpleName(), String.format("View %s is attached to presenter", mView.getClass().getSimpleName()));
    }

    /**
     * This method is called when the view is detached from this presenter.
     */
    protected void onViewDetached() {
        MVPLogger.d(getClass().getSimpleName(), String.format("View %s is detached to presenter", mView.getClass().getSimpleName()));
    }

    /**
     * Gets the view tied to this presenter.
     *
     * @return The attached view or null if the view is not attached yet.
     */
    @Nullable
    public final V getView() {
        return mView;
    }

    /**
     * This method should be called when the presenter is created.
     *
     * @param bundle Bundle The bundle used to save presenter data, may be null.
     */
    public final void create(@Nullable Bundle bundle) {
        onCreate(bundle);
    }

    /**
     * Destroys the presenter.
     */
    public final void destroy() {
        onDestroy();
    }

    /**
     * Saves presenter data in the given bundle.
     *
     * @param state Bundle to save state into.
     */
    public final void save(@NonNull Bundle state) {
        onSave(state);
    }

    /**
     * Attaches the view to the presenter.
     *
     * @param view View to attach.
     */
    public final void attachView(@NonNull V view) {
        mView = view;
        onViewAttached(view);
    }

    /**
     * Detaches the view from the presenter.
     */
    public final void detachView() {
        onViewDetached();
        mView = null;
    }
}
