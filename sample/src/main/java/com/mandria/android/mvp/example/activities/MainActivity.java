package com.mandria.android.mvp.example.activities;

import com.mandria.android.mvp.basecompatviews.BasePresenterActivity;
import com.mandria.android.mvp.example.MVPApplication;
import com.mandria.android.mvp.example.R;
import com.mandria.android.mvp.example.mvp.presenters.MainPresenter;
import com.mandria.android.mvp.example.mvp.views.MainView;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

/**
 * Created by michael on 13/04/2017.
 */
public class MainActivity extends BasePresenterActivity<MainPresenter> implements MainView {

    private TextView mResultTextView;

    @Override
    protected void injectActivity() {
        MVPApplication.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    protected MainPresenter instantiatePresenter() {
        return new MainPresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResultTextView = (TextView) findViewById(R.id.result_text);

        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResultTextView.setText(null);
                getPresenter().doStuff();
            }
        });
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
