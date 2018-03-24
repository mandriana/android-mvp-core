package com.mandria.android.mvp.rx.callbacks;

import android.support.annotation.NonNull;

import rx.functions.Action2;

/**
 * Interface for onError handler.
 */
public interface OnError<U> extends Action2<U, Throwable> {

    @Override
    void call(@NonNull U u, Throwable throwable);
}
