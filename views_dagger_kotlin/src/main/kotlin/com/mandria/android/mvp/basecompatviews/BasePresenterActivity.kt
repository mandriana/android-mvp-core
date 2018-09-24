package com.mandria.android.mvp.basecompatviews

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mandria.android.mvp.HasPresenter
import com.mandria.android.mvp.Presenter
import com.mandria.android.mvp.provider.PresenterProvider
import javax.inject.Inject


abstract class BasePresenterActivity<P : Presenter<*>> : AppCompatActivity(), HasPresenter<P> {

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

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBundle(
            PresenterProvider.CONTROLLER_STATE_KEY,
            presenterProvider.onSaveInstanceState()
        )
    }

    override fun onResume() {
        super.onResume()
        presenterProvider.attachViewToPresenter(this)
    }

    override fun onPause() {
        super.onPause()
        presenterProvider.detachViewFromPresenter(isFinishing)
    }

    override fun finish() {
        presenterProvider.detachViewFromPresenter(true)
        super.finish()
    }

    fun destroyPresenter() {
        presenterProvider.destroy()
    }
}