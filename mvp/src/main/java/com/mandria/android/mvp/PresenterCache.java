package com.mandria.android.mvp;

import android.util.LongSparseArray;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>A cache for the presenter.</p>
 * <p>This class should be a singleton or used with a dependency injector to be injected as an application singleton.</p>
 */
public class PresenterCache {

    private final AtomicLong mNextId;

    private final LongSparseArray<Presenter> mCache;

    /**
     * Constructor.
     */
    public PresenterCache() {
        mNextId = new AtomicLong();
        mCache = new LongSparseArray<>();
    }

    /**
     * Gets a presenter using its id.
     *
     * @param id  Presenter id.
     * @param <P> Presenter type.
     * @return The presenter of the corresponding id.
     */
    @SuppressWarnings("unchecked")
    <P> P getPresenter(Long id) {
        return (P) mCache.get(id);
    }

    /**
     * Saves a presenter.
     *
     * @param presenter Presenter to save.
     */
    void savePresenter(Presenter presenter) {
        mCache.put(mNextId.incrementAndGet(), presenter);
    }

    /**
     * Gets the id of a presenter, or null if presenter is not cached.
     *
     * @param presenter Presenter to retrieve the id from.
     * @return The presenter id or null.
     */
    Long getId(Presenter presenter) {
        int index = mCache.indexOfValue(presenter);
        if (index >= 0) {
            return mCache.keyAt(index);
        }
        return null;
    }

    /**
     * Removes a presenter from the cache.
     *
     * @param presenter Presenter to remove.
     */
    void removePresenter(Presenter presenter) {
        Long id = getId(presenter);
        if (id != null) {
            mCache.delete(id);
        }
    }

    /**
     * Removes all the presenter from the cache.
     */
    public void clear() {
        mCache.clear();
    }
}
