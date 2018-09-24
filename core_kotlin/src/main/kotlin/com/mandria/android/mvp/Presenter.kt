package com.mandria.android.mvp

import android.os.Bundle

/** Abstract class which defines a Presenter life cycle. **/
abstract class Presenter<out V : Any> {

    /** The view attached to the presenter. Is null when the view is detached. **/
    private var view: V? = null

    /**
     * This method is called when the presenter is created.
     * It is not attached to any "onCreate" method from Activity nor Fragment.
     * While the presenter remains in cache, this method would have been called only once.
     * The [savedState] is used when the presenter is re-created after the application
     * was destroyed by the system and then restored.
     */
    protected open fun onCreate(savedState: Bundle?) {
        MVPLogger.d(javaClass.simpleName, "On create presenter")
    }

    /** This method is called after the presenter is created and attached the first time to the view. **/
    open fun onCreatedThenAttached() {
        MVPLogger.d(javaClass.simpleName, "On presenter created then view attached")
    }

    /** This method is called when the user leaves the view. **/
    protected open fun onDestroy() {
        MVPLogger.d(javaClass.simpleName, "On destroy presenter")
    }

    /**
     * A returned state is the state that will be passed to [.onCreate] for a new presenter instance after a process restart.
     *
     * @param state Presenter bundle state.
     */
    protected open fun onSave(state: Bundle) {
        MVPLogger.d(javaClass.simpleName, "On save presenter state")
    }

    /** This method is called when the [view] is attached to this presenter. **/
    protected open fun onViewAttached(view: @UnsafeVariance V) {
        MVPLogger.d(
            javaClass.simpleName,
            String.format("View %s is attached to presenter", view.toString())
        )
    }

    /** This method is called when the view is detached from this presenter. **/
    protected open fun onViewDetached() {
        MVPLogger.d(
            javaClass.simpleName,
            String.format("View %s is detached from presenter", view?.toString())
        )
    }

    /** Gets the view tied to this presenter. **/
    fun getView(): V? {
        return view
    }

    /**
     * This method should be called when the presenter is created.
     * The bundle is used to save presenter data, it may be null.
     */
    fun create(bundle: Bundle?) {
        onCreate(bundle)
    }

    /** Destroys the presenter. **/
    fun destroy() {
        onDestroy()
    }

    /** Saves presenter state in the given bundle [state]. **/
    fun save(state: Bundle) {
        onSave(state)
    }

    /** Attaches the [view] to the presenter. **/
    fun attachView(view: @UnsafeVariance V) {
        this.view = view
        onViewAttached(view)
    }

    /** Detaches the view from the presenter. **/
    fun detachView() {
        onViewDetached()
        this.view = null
    }
}