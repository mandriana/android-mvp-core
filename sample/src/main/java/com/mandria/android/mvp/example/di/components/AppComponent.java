package com.mandria.android.mvp.example.di.components;

import com.mandria.android.mvp.di.CoreModule;
import com.mandria.android.mvp.example.activities.MainActivity;
import com.mandria.android.mvp.example.di.modules.AppModule;
import com.mandria.android.mvp.example.mvp.presenters.MainPresenter;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by michael on 19/04/2017.
 */
@Singleton
@Component(modules = {CoreModule.class, AppModule.class})
public interface AppComponent {

    void inject(MainActivity activity);

    void inject(MainPresenter presenter);
}
