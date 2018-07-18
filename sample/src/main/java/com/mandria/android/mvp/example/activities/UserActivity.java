package com.mandria.android.mvp.example.activities;

import com.mandria.android.mvp.PresenterClass;
import com.mandria.android.mvp.basecompatviews.BasePresenterActivity;
import com.mandria.android.mvp.example.R;
import com.mandria.android.mvp.example.mvp.presenters.UserPresenter;
import com.mandria.android.mvp.example.mvp.views.MainView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import dagger.android.AndroidInjection;

/**
 * Created by michael on 13/04/2017.
 */
@PresenterClass(UserPresenter.class)
public class UserActivity extends BasePresenterActivity<UserPresenter> implements MainView {

    private TextView mResultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResultTextView = findViewById(R.id.result_text);

        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResultTextView.setText(null);
                getPresenter().doStuff();
            }
        });

        Log.i("-----", getPresenter().getString());
    }

    @Override
    public void onTaskSuccess(String result) {
        mResultTextView.setText(result);
    }

    @Override
    public void onTaskFailed() {
        mResultTextView.setText("Task failed");
    }
}
