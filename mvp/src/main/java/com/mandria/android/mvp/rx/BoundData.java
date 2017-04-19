package com.mandria.android.mvp.rx;

import rx.Notification;

/**
 * This class binds a typed <View> view to some typed <Result> data.
 */
class BoundData<View, Result> {

    private final View mView;

    private final Notification<Result> mData;

    /**
     * Constructor.
     *
     * @param view View to bind.
     * @param data Data to bind.
     */
    BoundData(View view, Notification<Result> data) {
        mView = view;
        mData = data;
    }

    public View getView() {
        return mView;
    }

    public Notification<Result> getData() {
        return mData;
    }
}
