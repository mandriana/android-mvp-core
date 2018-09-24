package com.mandria.android.mvp.kotlin.example.di

import com.mandria.android.mvp.Presenter
import dagger.MapKey
import kotlin.reflect.KClass


/** Annotation to ensure [MapKey] values are [ViewModel] extensions. */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class PresenterKey(val value: KClass<out Presenter<*>>)