package com.mandria.android.mvp;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Presenter factory.
 */
@Singleton
public class PresenterFactory {

    private final Map<Class<? extends Presenter>, Provider<Presenter>> mCreators;

    @Inject
    public PresenterFactory(Map<Class<? extends Presenter>, Provider<Presenter>> creators) {
        mCreators = creators;
    }

    /**
     * Creates a fully injected presenter. If no presenter can be instantiated, an exception is thrown.
     *
     * @param presenterClass Presenter class.
     * @param <T>            Presenter instance.
     * @return Instance of presenter.
     */
    @SuppressWarnings("unchecked")
    public <T extends Presenter> T create(Class<T> presenterClass) {
        Provider<Presenter> presenterProvider = mCreators.get(presenterClass);
        if (presenterProvider == null) {
            for (Map.Entry<Class<? extends Presenter>, Provider<Presenter>> entry : mCreators.entrySet()) {
                if (presenterClass.isAssignableFrom(entry.getKey())) {
                    presenterProvider = entry.getValue();
                }
            }
        }

        if (presenterProvider == null) {
            throw new IllegalArgumentException("Class " + presenterClass + " seems not provided");
        }

        try {
            return (T) presenterProvider.get();
        } catch (Exception e) {
            MVPLogger.e("PresenterFactory", e.getMessage(), e);
            throw e;
        }
    }
}
