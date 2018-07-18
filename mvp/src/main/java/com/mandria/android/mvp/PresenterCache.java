package com.mandria.android.mvp;

import java.util.HashMap;

/**
 * <p>A cache for the presenter.</p>
 * <p>This class should be a singleton or used with a dependency injector to be injected as an application singleton.</p>
 */
public final class PresenterCache {

    private static final String TAG = "PresenterCache";

    private HashMap<String, Presenter> mIdToPresenter;

    private HashMap<Presenter, String> mPresenterToId;

    /**
     * Constructor.
     */
    public PresenterCache() {
        mIdToPresenter = new HashMap<>();
        mPresenterToId = new HashMap<>();
    }

    /**
     * Gets a presenter using its id.
     *
     * @param id  Presenter id.
     * @param <P> Presenter type.
     * @return The presenter of the corresponding id.
     */
    @SuppressWarnings("unchecked")
    <P> P getPresenter(String id) {
        return (P) mIdToPresenter.get(id);
    }

    /**
     * Saves a presenter.
     *
     * @param presenter Presenter to save.
     */
    void savePresenter(Presenter presenter) {
        String id = presenter.getClass().getSimpleName() + "/" + System.nanoTime() + "/" + (int) (Math.random() * Integer.MAX_VALUE);

        MVPLogger.d(TAG, String.format("Saving presenter %s to cache with id %s", presenter.getClass().getSimpleName(), id));

        mIdToPresenter.put(id, presenter);
        mPresenterToId.put(presenter, id);
    }

    /**
     * Gets the id of a presenter, or null if presenter is not cached.
     *
     * @param presenter Presenter to retrieve the id from.
     * @return The presenter id or null.
     */
    String getId(Presenter presenter) {
        return mPresenterToId.get(presenter);
    }

    /**
     * Removes a presenter from the cache.
     *
     * @param presenter Presenter to remove.
     */
    void removePresenter(Presenter presenter) {
        MVPLogger.d(TAG, String.format("Removing presenter %s from cache", presenter.getClass().getSimpleName()));

        mIdToPresenter.remove(mPresenterToId.get(presenter));
    }

    /**
     * Removes all the presenter from the cache.
     */
    public void clear() {
        mPresenterToId.clear();
        mIdToPresenter.clear();
    }
}
