package com.mandria.android.mvp.rx.callbacks;

import io.reactivex.functions.BiConsumer;

/**
 * Interface for onNext handler.
 */
public interface OnNext<U, V> extends BiConsumer<U, V> {

}
