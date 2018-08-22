package com.mandria.android.mvp.rx.callbacks;

import android.support.annotation.NonNull;

import io.reactivex.functions.Consumer;

/**
 * Interface for onCompleted handler.
 */
public interface OnCompleted<U> extends Consumer<U> {

    @Override
    void accept(@NonNull U u) throws Exception;
}
