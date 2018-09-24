package com.mandria.android.mvp

/** Interface which should be implemented by the view which will contain a presenter. **/
interface HasPresenter<P : Presenter<*>> {

    /** Presenter instance. **/
    val presenter: P
}