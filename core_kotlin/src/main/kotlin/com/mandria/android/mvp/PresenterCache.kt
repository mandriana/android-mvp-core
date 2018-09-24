package com.mandria.android.mvp


/**
 * A cache for the presenters.
 * This class should be a singleton or used with a dependency injector to be injected as an application singleton.
 */
class PresenterCache {

    private val idToPresenter: HashMap<String, Presenter<*>> = HashMap()

    private val presenterToId: HashMap<Presenter<*>, String> = HashMap()

    operator fun get(id: String) = idToPresenter[id]

    /**
     * Gets a presenter using its id.
     *
     * @param id  Presenter id.
     * @param <P> Presenter type.
     * @return The presenter of the corresponding id.
     */
    @Suppress("UNCHECKED_CAST")
    fun <P> getPresenter(id: String): P = idToPresenter[id] as P

    /** Saves a [presenter] in the cache. **/
    fun savePresenter(presenter: Presenter<*>) {
        val id =
            "${presenter.javaClass.simpleName}/${System.nanoTime()}/${(Math.random() * Int.MAX_VALUE).toInt()}"

        MVPLogger.d(TAG, "Saving presenter ${presenter.javaClass.simpleName} to cache with id $id")

        idToPresenter[id] = presenter
        presenterToId[presenter] = id
    }

    /**
     * Gets the id of a [presenter], or null if [presenter] is not cached.
     *
     * @return The presenter id or null.
     */
    fun getId(presenter: Presenter<*>): String? {
        return presenterToId[presenter]
    }

    /** Removes a [presenter] from the cache. **/
    fun removePresenter(presenter: Presenter<*>) {
        MVPLogger.d(TAG, "Removing presenter ${presenter.javaClass.simpleName} from cache")
        idToPresenter.remove(presenterToId.remove(presenter))
    }

    /** Removes all the presenter from the cache. **/
    fun clear() {
        presenterToId.clear()
        idToPresenter.clear()
    }

    companion object {
        const val TAG = "PresenterCache"
    }
}