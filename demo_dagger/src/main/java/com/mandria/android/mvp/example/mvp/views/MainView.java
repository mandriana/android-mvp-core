package com.mandria.android.mvp.example.mvp.views;


public interface MainView {
    void onTaskSuccess(String result);

    void onTaskComplete();

    void onTaskFailed();
}
