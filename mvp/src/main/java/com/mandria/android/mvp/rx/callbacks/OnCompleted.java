package com.mandria.android.mvp.rx.callbacks;

import android.support.annotation.NonNull;

import rx.functions.Action1;

/**
 * Interface for onCompleted handler.
 */
public interface OnCompleted<U> extends Action1<U> {

    @Override
    void call(@NonNull U u);
}
