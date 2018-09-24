package com.mandria.android.mvp.provider

import android.os.Bundle
import com.mandria.android.mvp.HasPresenter
import com.mandria.android.mvp.Presenter
import com.mandria.android.mvp.PresenterCache
import javax.inject.Inject

/** This class provides and attaches a presenter to a view. **/
class PresenterProvider @Inject constructor(
    private val presenterCache: PresenterCache,
    private val presenterFactory: PresenterFactory
) {

    private var bundle: Bundle? = null

    private var presenter: Presenter<*>? = null

    private var presenterHasView = false

    /**
     * Loads the presenter from cache if available.
     * Return true if presenter was retrieved from cache, else false.
     */
    private fun loadPresenterFromCache(): Boolean {
        if (presenter == null) {
            bundle?.let { b ->
                b.getString(PRESENTER_ID_KEY)?.let { presenter = presenterCache[it] }
            }
        }

        return presenter != null
    }

    /** Creates the presenter using the presenter factory. **/
    private fun createPresenter(presenterClass: Class<out Presenter<*>>) {
        if (presenter == null) {
            presenter = (presenterFactory.create(presenterClass)).apply {
                presenterCache.savePresenter(this)
                create(bundle?.getBundle(PRESENTER_BUNDLE_KEY))
            }
        }
    }

    /**
     * Prepares the presenter by retrieving it from the cache or instantiating a new one using the factory.
     * The presenter to retrieve is known through the [PresenterClass] annotation that each
     * presenter owner ([HasPresenter]) must specify.
     * An [IllegalArgumentException] is thrown if the annotation is not specified.
     */
    fun <P : Presenter<*>> preparePresenter(presenterOwner: HasPresenter<P>) {
        val annotation: PresenterClass =
            presenterOwner.javaClass.annotations.find { it is PresenterClass } as? PresenterClass
                ?: throw IllegalArgumentException("HasPresenter owner does not provide @PresenterClass annotation")
        if (!loadPresenterFromCache()) {
            createPresenter(annotation.value.java)
        }
    }

    /** Allows the presenter to save its state. **/
    fun onSaveInstanceState(): Bundle {
        val controllerBundle = Bundle()

        presenter?.let {
            // Stores the presenter bundle in this controller bundle
            val presenterBundle = Bundle()
            it.save(presenterBundle)
            controllerBundle.putBundle(PRESENTER_BUNDLE_KEY, presenterBundle)

            // Saves the presenter id in the bundle to reattach the view to the presenter
            controllerBundle.putString(PRESENTER_ID_KEY, presenterCache.getId(it))
        }

        return controllerBundle
    }

    /** Allows the presenter to restore its state. **/
    fun onRestoreInstanceState(presenterState: Bundle?) {
        bundle = presenterState
    }

    /** Attaches the view to the presenter. **/
    fun attachViewToPresenter(view: Any) {
        if (!presenterHasView) {
            presenter?.let { p ->
                if (p.getView() == null) {
                    p.attachView(view)
                    if (!presenterHasView)
                        p.onCreatedThenAttached()
                    presenterHasView = true
                }
            }
                ?: throw IllegalStateException("Call preparePresenter() before attaching view to presenter")
        }
    }

    /** Detaches the view from the presenter and destroys it if needed using the [destroy] boolean. **/
    fun detachViewFromPresenter(destroy: Boolean) {
        // Presenter can be null if doing back :
        // - onPause is called
        // - finish is called
        presenter?.let { p ->
            if (presenterHasView) {
                p.detachView()
                presenterHasView = false
            }
            if (destroy) {
                p.destroy()
                presenterCache.removePresenter(p)
                presenter = null
            }
        }
    }

    /** Calls [detachViewFromPresenter] with true param. **/
    fun destroy() {
        detachViewFromPresenter(true)
    }

    /**
     * Gets the presenter or throw an [IllegalStateException] if [preparePresenter] was not called
     * before accessing the presenter in the view.
     */
    @Suppress("UNCHECKED_CAST")
    fun <P : Presenter<*>> getPresenter(): P {
        return presenter as? P
            ?: throw IllegalStateException("Call preparePresenter() before accessing presenter")
    }

    companion object {
        const val CONTROLLER_STATE_KEY = "com.mandria.android.mvp.presenter.controller.state"

        private const val PRESENTER_BUNDLE_KEY = "com.mandria.android.mvp.presenter.bundle"

        private const val PRESENTER_ID_KEY = "com.mandria.android.mvp.presenter.id"
    }
}