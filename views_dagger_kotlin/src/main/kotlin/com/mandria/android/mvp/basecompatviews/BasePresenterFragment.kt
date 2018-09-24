package com.mandria.android.mvp.basecompatviews

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.mandria.android.mvp.HasPresenter
import com.mandria.android.mvp.Presenter
import com.mandria.android.mvp.provider.PresenterProvider
import javax.inject.Inject

abstract class BasePresenterFragment<P : Presenter<*>> : Fragment(), HasPresenter<P> {

    @Inject
    lateinit var presenterProvider: PresenterProvider

    override val presenter: P
        get() = presenterProvider.getPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            presenterProvider.onRestoreInstanceState(it.getBundle(PresenterProvider.CONTROLLER_STATE_KEY))
        }

        presenterProvider.preparePresenter(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(
            PresenterProvider.CONTROLLER_STATE_KEY,
            presenterProvider.onSaveInstanceState()
        )
    }

    override fun onResume() {
        super.onResume()
        presenterProvider.attachViewToPresenter(this)
    }

    override fun onPause() {
        presenterProvider.detachViewFromPresenter(false)
        super.onPause()
    }

    override fun onDestroy() {
        presenterProvider.detachViewFromPresenter(activity?.isChangingConfigurations ?: true)
        super.onDestroy()
    }

    /**
     * Forces the destruction of a presenter :
     * - presenter will be notified of their destruction with [Presenter.detachView] first then [Presenter.destroy].
     * - presenter will be removed from the cache
     */
    fun destroyPresenter() {
        presenterProvider.destroy()
    }
}