package com.mandria.android.mvp.provider

import com.mandria.android.mvp.Presenter
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/** Annotation to specify presenter classes to use as key with dagger binding. **/
@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PresenterClass(val value: KClass<out Presenter<*>>)