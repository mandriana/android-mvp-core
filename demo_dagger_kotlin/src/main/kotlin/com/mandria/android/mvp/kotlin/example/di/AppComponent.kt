package com.mandria.android.mvp.kotlin.example.di

import android.app.Application
import com.mandria.android.mvp.di.CoreModule
import com.mandria.android.mvp.kotlin.example.activities.ListDatabasesActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AppModule::class, CoreModule::class, SingletonPresenterModule::class]
)
interface AppComponent {

    fun inject(listDatabasesActivity: ListDatabasesActivity)

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