package com.mandria.android.mvp.rx.callbacks;

import android.support.annotation.NonNull;

import io.reactivex.functions.BiConsumer;

/**
 * Interface for onNext handler.
 */
public interface OnNext<U, V> extends BiConsumer<U, V> {

    @Override
    void accept(@NonNull U u, V v) throws Exception;
}
