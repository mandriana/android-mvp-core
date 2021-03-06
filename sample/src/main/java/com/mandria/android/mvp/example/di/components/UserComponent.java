package com.mandria.android.mvp.example.di.components;

import com.mandria.android.mvp.example.activities.UserActivity;
import com.mandria.android.mvp.example.di.annotations.UserScope;
import com.mandria.android.mvp.example.di.modules.UserModule;
import com.mandria.android.mvp.example.di.modules.UserPresenterModule;

import dagger.Subcomponent;

@UserScope
@Subcomponent(modules = {
        UserModule.class,
        UserPresenterModule.class
})
public interface UserComponent {

    void inject(UserActivity userActivity);

    @Subcomponent.Builder
    interface Builder {

        UserComponent build();
    }
}
