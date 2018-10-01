package com.mandria.android.mvp.example.di.components;

import com.mandria.android.mvp.di.CoreModule;
import com.mandria.android.mvp.example.MVPApplication;
import com.mandria.android.mvp.example.activities.MainActivity;
import com.mandria.android.mvp.example.di.modules.AppModule;
import com.mandria.android.mvp.example.di.modules.PresenterModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;


@Singleton
@Component(modules = {
        CoreModule.class,
        AppModule.class,
        PresenterModule.class
})
public interface AppComponent {

    void inject(MainActivity mainActivity);

    @Component.Builder
    interface Builder {

        /**
         * Add the application object to the graph.
         */
        @BindsInstance
        Builder application(MVPApplication application);

        /**
         * Build the component.
         */
        AppComponent build();
    }
}
