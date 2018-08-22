package com.mandria.android.mvp.provider;

import com.mandria.android.mvp.Presenter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to ensure only presenters can be provided by the {@link PresenterFactory}.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PresenterClass {

    /**
     * @return Class type of a presenter class.
     */
    Class<? extends Presenter> value();
}
