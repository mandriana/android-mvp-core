package com.mandria.android.mvp.rx.callbacks;

import androidx.annotation.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Interface for onCompleted handler.
 */
public interface OnCompleted<U> extends Consumer<U> {

    @Override
    void accept(@NonNull U u) throws Exception;
}
