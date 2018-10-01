package com.mandria.android.mvp.kotlin.example.di

import android.app.Application
import com.mandria.android.mvp.di.CoreModule
import com.mandria.android.mvp.kotlin.example.activities.MainActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AppModule::class, CoreModule::class, PresenterModule::class]
)
interface AppComponent {

    fun inject(mainActivity: MainActivity)

    /** Component builder which only needs the application objects as dependency for the modules. */
    @Component.Builder
    interface Builder {

        /** Add the application object to the graph. */
        @BindsInstance
        fun application(application: Application): Builder

        /** Build the component. */
        fun build(): AppComponent
    }
}