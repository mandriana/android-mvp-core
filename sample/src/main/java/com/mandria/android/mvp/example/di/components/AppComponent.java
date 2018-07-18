package com.mandria.android.mvp.example.di.components;

import com.mandria.android.mvp.di.CoreModule;
import com.mandria.android.mvp.example.MVPApplication;
import com.mandria.android.mvp.example.di.modules.ActivityBindingModule;
import com.mandria.android.mvp.example.di.modules.AppModule;
import com.mandria.android.mvp.example.di.modules.PresenterModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

/**
 * Created by michael on 19/04/2017.
 */
@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        ActivityBindingModule.class,
        CoreModule.class,
        AppModule.class,
        PresenterModule.class
})
public interface AppComponent {

    void inject(MVPApplication application);

    UserComponent.Builder createUserComponent();

    @Component.Builder
    interface Builder {

        /** Add the application object to the graph. */
        @BindsInstance
        Builder application(MVPApplication application);

        /** Build the component. */
        AppComponent build();
    }
}
