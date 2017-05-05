package com.mandria.android.mvp.rx.callbacks;

import io.reactivex.functions.BiConsumer;

/**
 * Interface for onError handler.
 */
public interface OnError<U> extends BiConsumer<U, Throwable> {

}
