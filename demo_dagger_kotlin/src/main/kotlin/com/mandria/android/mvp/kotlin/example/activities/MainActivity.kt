package com.mandria.android.mvp.kotlin.example.activities

import android.os.Bundle
import com.mandria.android.mvp.basecompatviews.BasePresenterActivity
import com.mandria.android.mvp.kotlin.example.MVPApplication
import com.mandria.android.mvp.kotlin.example.R
import com.mandria.android.mvp.kotlin.example.mvp.presenter.MainPresenter
import com.mandria.android.mvp.kotlin.example.mvp.view.MainView
import com.mandria.android.mvp.provider.PresenterClass
import kotlinx.android.synthetic.main.activity_main.*


@PresenterClass(MainPresenter::class)
class MainActivity : BasePresenterActivity<MainPresenter>(), MainView {

    override fun onCreate(savedInstanceState: Bundle?) {
        MVPApplication.appComponent.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton.setOnClickListener {
            resultTV.text = "Task started"
            completeTV.text = null
            presenter.doStuff()
        }
    }

    override fun onTaskSuccess(result: String) {
        resultTV.text = result
    }

    override fun onTaskComplete() {
        completeTV.text = "Task completed"
    }

    override fun onTaskFailed() {
        resultTV.text = "Task failed"
    }
}