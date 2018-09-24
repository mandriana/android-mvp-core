package com.mandria.android.mvp.provider

import com.mandria.android.mvp.MVPLogger
import com.mandria.android.mvp.Presenter
import javax.inject.Inject
import javax.inject.Provider

/** Presenter factory from bound presenters. **/
class PresenterFactory @Inject constructor(
    private val creators: Map<Class<out Presenter<*>>, @JvmSuppressWildcards Provider<Presenter<*>>>
) {

    /** Get a presenter from the corresponding [Provider] with all constructor dependencies resolved. **/
    fun <T : Class<out Presenter<*>>> create(modelClass: T): Presenter<*> {
        val creator: Provider<Presenter<*>> = creators[modelClass] ?: creators.entries.firstOrNull {
            modelClass.isAssignableFrom(it.key)
        }?.value
        ?: throw IllegalArgumentException("Class $modelClass seems not provided in ViewModelModule")

        try {
            return creator.get() as Presenter<*>
        } catch (e: Exception) {
            MVPLogger.e("PresenterFactory", "ViewModel instance cannot be provided")
            throw e
        }
    }
}