package com.mandria.android.mvp.rx.callbacks;

import android.support.annotation.NonNull;

import io.reactivex.functions.BiConsumer;

/**
 * Interface for onError handler.
 */
public interface OnError<U> extends BiConsumer<U, Throwable> {

    @Override
    void accept(@NonNull U u, Throwable throwable) throws Exception;
}
