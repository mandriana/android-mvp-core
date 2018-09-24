package com.mandria.android.mvp.kotlin.example.activities

import android.os.Bundle
import com.mandria.android.mvp.basecompatviews.BasePresenterActivity
import com.mandria.android.mvp.kotlin.example.MVPApplication
import com.mandria.android.mvp.kotlin.example.mvp.presenter.ListDatabasePresenter
import com.mandria.android.mvp.provider.PresenterClass


@PresenterClass(ListDatabasePresenter::class)
class ListDatabasesActivity : BasePresenterActivity<ListDatabasePresenter>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        MVPApplication.appComponent.inject(this)

        super.onCreate(savedInstanceState)

        presenter.insertDatabase()
    }

}