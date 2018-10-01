package com.mandria.android.mvp.example.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mandria.android.mvp.basecompatviews.BasePresenterActivity;
import com.mandria.android.mvp.example.MVPApplication;
import com.mandria.android.mvp.example.R;
import com.mandria.android.mvp.example.mvp.presenters.MainPresenter;
import com.mandria.android.mvp.example.mvp.views.MainView;
import com.mandria.android.mvp.provider.PresenterClass;

@PresenterClass(MainPresenter.class)
public class MainActivity extends BasePresenterActivity<MainPresenter> implements MainView {

    private TextView mResultTextView;
    private TextView mCompleteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MVPApplication.mAppComponent.inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResultTextView = findViewById(R.id.result_text);
        mCompleteTextView = findViewById(R.id.complete_text);

        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResultTextView.setText("Task started");
                mCompleteTextView.setText(null);
                getPresenter().doStuff();
            }
        });
    }

    @Override
    public void onTaskSuccess(String result) {
        mResultTextView.setText(result);
    }

    @Override
    public void onTaskComplete() {
        mCompleteTextView.setText("Task completed");
    }

    @Override
    public void onTaskFailed() {
        mResultTextView.setText("Task failed");
    }
}
