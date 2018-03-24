package com.mandria.android.mvp.rx.callbacks;

import android.support.annotation.NonNull;

import rx.functions.Action2;

/**
 * Interface for onNext handler.
 */
public interface OnNext<U, V> extends Action2<U, V> {

    @Override
    void call(@NonNull U u, V v);
}
