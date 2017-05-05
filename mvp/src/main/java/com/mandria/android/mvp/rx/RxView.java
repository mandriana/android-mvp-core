package com.mandria.android.mvp.rx;

import android.support.annotation.Nullable;

/**
 * A view wrapper class to handle view which is nullable.
 */
public class RxView<V> {

    public final V view;

    RxView(@Nullable V view) {
        this.view = view;
    }

    @Override
    public String toString() {
        return String.format("RxView is : %s", view);
    }

    @Override
    public int hashCode() {
        return view != null ? view.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        RxView<?> other = (RxView<?>) obj;

        if (view != null) {
            return view.equals(other.view);
        }
        return other.view == null;
    }
}
