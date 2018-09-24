package com.mandria.android.mvp.rx.callbacks

import io.reactivex.functions.BiConsumer


/** Interface for onError handler. **/
interface OnError<U> : BiConsumer<U, Throwable> {

    @Throws(Exception::class)
    override fun accept(u: U, throwable: Throwable)
}