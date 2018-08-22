package com.mandria.android.mvp.example.di.modules;

import com.mandria.android.mvp.example.di.annotations.UserScope;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class UserModule {

    @Provides
    @UserScope
    @Named("TestString")
    public String getString() {
        return "Injected";
    }
}
