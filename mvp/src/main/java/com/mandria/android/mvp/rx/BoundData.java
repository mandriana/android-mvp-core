package com.mandria.android.mvp.rx;

import io.reactivex.Notification;

/**
 * This class binds a typed <View> view to some typed <Result> data.
 */
public class BoundData<View, Result> {

    private final View mView;

    private final Notification<Result> mData;

    /**
     * Constructor.
     *
     * @param view View to bind.
     * @param data Data to bind.
     */
    public BoundData(View view, Notification<Result> data) {
        mView = view;
        mData = data;
    }

    /**
     * @return The view attached to the data.
     */
    public View getView() {
        return mView;
    }

    /**
     * @return The notification from the observable.
     */
    public Notification<Result> getData() {
        return mData;
    }
}
