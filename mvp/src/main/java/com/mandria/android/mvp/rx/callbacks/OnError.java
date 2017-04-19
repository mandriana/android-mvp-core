package com.mandria.android.mvp.rx.callbacks;

import rx.functions.Action2;

/**
 * Interface for onError handler.
 */
public interface OnError<U> extends Action2<U, Throwable> {

}
