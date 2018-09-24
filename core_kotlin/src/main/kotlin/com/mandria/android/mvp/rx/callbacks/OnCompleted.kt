package com.mandria.android.mvp.rx.callbacks

import io.reactivex.functions.Consumer

/** Interface for onCompleted handler. **/
interface OnCompleted<U> : Consumer<U> {

    @Throws(Exception::class)
    override fun accept(u: U)
}