package com.mandria.android.mvp.example.di.annotations;

import com.mandria.android.mvp.Presenter;

import dagger.MapKey;

/** Annotation to ensure [MapKey] values are [ViewModel] extensions. */
@MapKey
public @interface PresenterKey {

    Class<? extends Presenter> value();
}
