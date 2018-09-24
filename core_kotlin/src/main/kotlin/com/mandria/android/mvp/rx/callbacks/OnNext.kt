package com.mandria.android.mvp.rx.callbacks

import io.reactivex.functions.BiConsumer


/** Interface for onNext handler. **/
interface OnNext<U, V> : BiConsumer<U, V> {

    @Throws(Exception::class)
    override fun accept(u: U, v: V)
}