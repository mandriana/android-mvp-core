package com.mandria.android.mvp.rx

import android.os.Bundle
import androidx.annotation.CallSuper
import com.mandria.android.mvp.MVPLogger
import com.mandria.android.mvp.Presenter
import com.mandria.android.mvp.rx.callbacks.OnCompleted
import com.mandria.android.mvp.rx.callbacks.OnError
import com.mandria.android.mvp.rx.callbacks.OnNext
import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.ReplaySubject
import java.util.concurrent.ConcurrentHashMap

open class RxPresenter<V : Any> : Presenter<V>() {

    private val classTag: String = javaClass.simpleName

    /** Replay subject to fire the cache removal of all awaiting task. **/
    private val cacheSynchronization = ReplaySubject.create<Boolean>()

    /** Cache synchronization replay subject disposable. **/
    private var cacheSynchronizationDisposable: Disposable? = null

    /**
     * Queue to store the observable tags which have terminated while manipulating the cache.
     * This list is used to avoid ConcurrentModificationException on the cache.
     */
    private val terminatedQueue = ArrayList<String>()

    /**
     * Behaviour subject to publish the view state through observable operations.
     * View state is wrapped in a [RxView] since BehaviorSubject cannot emit null.
     */
    private val viewSubject = BehaviorSubject.create<RxView<V>>()

    /**
     * Map of cached observables.
     * Operations on this map should be synchronized to avoid concurrency access.
     */
    private val cache = ConcurrentHashMap<String, CacheableStream<V, *>>()

    /** Stores the subscriptions to release them in [destroy] call. **/
    private var disposables: CompositeDisposable? = null

    /** Actions queue. **/
    private val queue = HashMap<String, Consumer<V>>()

    @CallSuper
    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        disposables = CompositeDisposable()

        // The replay subject must be initialized when presenter is created
        cacheSynchronization.onNext(false)

        cacheSynchronizationDisposable = cacheSynchronization.subscribe { manipulating ->
            if (!manipulating && !terminatedQueue.isEmpty()) {
                val listIterator = terminatedQueue.listIterator()
                while (listIterator.hasNext()) {
                    val tag = listIterator.next()
                    removeFromCache(tag)
                    listIterator.remove()
                }
            }
        }
    }

    @CallSuper
    override fun onViewAttached(view: V) {
        super.onViewAttached(view)

        viewSubject.onNext(RxView(view))
        resumeQueue(view)
        resumeAll()
    }

    @CallSuper
    override fun onViewDetached() {
        super.onViewDetached()

        viewSubject.onNext(RxView(null))
        disposeAll()
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()

        viewSubject.onComplete()
        cancelAll()
        cacheSynchronization.onComplete()
        cacheSynchronizationDisposable?.dispose()
    }

    /** Disposes from all subscribed streams. **/
    private fun disposeAll() {
        disposables?.let {
            it.clear()
            disposables = CompositeDisposable()
        }

        cacheSynchronization.onNext(true)

        for (key in cache.keys()) {
            cache[key]?.dispose()
        }

        cacheSynchronization.onNext(false)
    }

    /** Cancels all running streams by disposing from them and clearing the cache. **/
    protected fun cancelAll() {
        disposables?.let {
            it.clear()
            disposables = CompositeDisposable()
        }

        cacheSynchronization.onNext(true)

        for (key in cache.keys()) {
            cache[key]?.cancel()
        }
        cache.clear()

        cacheSynchronization.onNext(false)
    }

    /** Resumes all cached observables. **/
    private fun resumeAll() {
        cacheSynchronization.onNext(true)

        for (key in cache.keys()) {
            cache[key]?.resume()
        }

        cacheSynchronization.onNext(false)
    }

    /** Cancels a cached observable identified the its [tag]. **/
    protected fun cancel(tag: String) {
        cache[tag]?.let {
            it.cancel()
            cache.remove(tag)
        }
    }

    /** Removes an observable from the cache identified the its [tag]. **/
    private fun removeFromCache(tag: String) {
        MVPLogger.d(classTag, String.format("Remove %s from cache", tag))
        cache.remove(tag)
    }

    /**
     * Every disposable generated outside this base class should be added here in order to
     * avoid memory leak.
     *
     * The subscriptions will be disposed in [onViewDetached] callback.
     *
     * @param disposable Observable subscription.
     */
    fun addDisposable(disposable: Disposable) {
        disposables?.add(disposable)
    }

    /** Removes a previously registered [disposable]. **/
    fun removeDisposable(disposable: Disposable) {
        disposables?.remove(disposable)
    }

    /**
     * Gets a consumer for the stream which will dispatch events to each corresponding callback.
     *
     * Maybe, Single and Completable are transformed to Observable to be materialized.
     * They will use [onNext], [onError] and [onCompleted] actions according to their specification.
     *
     * @param onNext      OnNext action to call
     * @param onError     OnError action to call
     * @param onCompleted OnCompleted action to call
     * @param <Result>    Result type of the observable.
     * @return The consumer to attach to the stream.
     */
    private fun <Result> getCacheableStreamConsumer(
        tag: String,
        onNext: OnNext<V, Result>?,
        onError: OnError<V>?,
        onCompleted: OnCompleted<V>?
    ): Consumer<BoundData<V, Result>> {
        return Consumer { rxViewResultBoundData ->
            val view = rxViewResultBoundData.view
            val notification = rxViewResultBoundData.data

            // Consumer will be notified when view is not null
            when {
                notification.isOnNext -> onNext?.accept(view!!, notification.value!!)
                notification.isOnComplete -> onCompleted?.accept(view!!)
                notification.isOnError -> onError?.accept(view!!, notification.error!!)
            }

            if (notification.isOnComplete || notification.isOnError) {
                if (cacheSynchronization.value)
                    terminatedQueue.add(tag)
                else
                    removeFromCache(tag)
            }
        }
    }

    /**
     * Calls the [action] once view is attached.
     *
     * The [tag] is used to remove the action from the task queue if not started yet.
     * If the [action] starts a task with the same tag passed to this function, the task will be
     * cancelled in case it has already started. The cancellation is done with [cancel] method,
     * passing it the [tag].
     *
     * This is intended to be used for instance when Activity.onRequestPermissionsResult(int, String[], int[])
     * need to start a task once view is attached.
     *
     * @param tag      Action tag (ideally same as stream tag if a stream should be started in the action param).
     * @param action Action to invoke once view is attached.
     */
    fun startOnViewAttached(tag: String, action: Consumer<V>) {
        viewSubject.value?.view?.let {
            try {
                action.accept(it)
            } catch (e: Exception) {
                MVPLogger.e(classTag, e.message ?: e.toString())
            }
        } ?: run { queue[tag] = action }
    }

    /**
     * Removes the action from the queue with the ability to cancel a task that as started with the same tag.
     *
     * @param tag             Action tag (ideally same as observable tag if an observable should be started in the action0 param).
     * @param cancelIfStarted Tries to cancel the task if any matching the given tag.
     */
    fun cancelWaitingForViewAttached(tag: String, cancelIfStarted: Boolean) {
        queue.remove(tag)
        if (cancelIfStarted) {
            cancel(tag)
        }
    }

    /** Resumes the queue. */
    private fun resumeQueue(view: V) {
        if (!queue.isEmpty()) {
            MVPLogger.d(classTag, "${queue.size} action waited for view attached to start")
            val queueIterator = queue.entries.iterator()
            while (queueIterator.hasNext()) {
                val next = queueIterator.next()
                try {
                    MVPLogger.d(classTag, "Calling action for tag : ${next.key}")
                    next.value.accept(view)
                } catch (e: Exception) {
                    MVPLogger.e(classTag, e.message ?: e.toString())
                }
                queueIterator.remove()
            }
        }
    }

    /** Gets if a task identified by its [tag] is still running by checking if it exists in the cache. **/
    fun isTaskInProgress(tag: String): Boolean {
        return cache.containsKey(tag)
    }

    /**
     * Starts an [observable].
     *
     * If an existing observable with the same [tag] exists in cache, the observable will be resumed.
     * Otherwise it will be added in the cache and started.
     *
     * The [withDefaultSchedulers] parameter is used to attach or not the observable to default schedulers
     * using [observableIOSchedulerTransformer].
     *
     * @param tag                   Observable tag.
     * @param observable            Observable to start.
     * @param onNext                OnNext action to call
     * @param onError               OnError action to call
     * @param onCompleted           OnCompleted action to call
     * @param withDefaultSchedulers True if default schedulers should be applied.
     * @param <Result>              Result type of the observable.
     */
    @Suppress("UNCHECKED_CAST")
    fun <Result> start(
        tag: String,
        observable: Observable<Result>,
        onNext: OnNext<V, Result>,
        onError: OnError<V>,
        onCompleted: OnCompleted<V>? = null,
        withDefaultSchedulers: Boolean = true
    ) {
        var finalObservable = observable
        var cached = cache[tag] as? CacheableStream<V, Result>

        if (!cache.containsKey(tag)) {
            MVPLogger.d(classTag, "Starting task : $tag")
            if (withDefaultSchedulers) {
                finalObservable = finalObservable.compose(applyObservableIOScheduler())
            }
            cached = CacheableStream(
                finalObservable,
                viewSubject,
                getCacheableStreamConsumer(tag, onNext, onError, onCompleted)
            )

            if (!cache.containsKey(tag)) {
                cache[tag] = cached
            } else {
                cached.cancel()
                cached = cache[tag] as? CacheableStream<V, Result>
            }
        } else {
            MVPLogger.d(classTag, "Resuming task : $tag")
        }

        cached?.resume()
    }

    @Suppress("UNCHECKED_CAST")
    fun <Result> start(
        tag: String,
        observable: Observable<Result>,
        onNext: (V, Result) -> Unit,
        onError: (V, Throwable) -> Unit,
        onCompleted: ((V) -> Unit)? = null,
        withDefaultSchedulers: Boolean = true
    ) {
        start(
            tag, observable,
            object : OnNext<V, Result> {
                override fun accept(u: V, v: Result) {
                    onNext(u, v)
                }
            },
            object : OnError<V> {
                override fun accept(u: V, throwable: Throwable) {
                    onError(u, throwable)
                }
            },
            onCompleted?.let {
                object : OnCompleted<V> {
                    override fun accept(u: V) {
                        it(u)
                    }
                }
            },
            withDefaultSchedulers
        )
    }

    /**
     * Starts a [flowable].
     *
     * If an existing flowable with the same [tag] exists in cache, the flowable will be resumed.
     * Otherwise it will be added in the cache and started.
     *
     * The [withDefaultSchedulers] parameter is used to attach or not the flowable to default schedulers
     * using [observableIOSchedulerTransformer]
     *
     * @param tag                   Flowable tag.
     * @param flowable              Flowable to start.
     * @param onNext                OnNext action to call
     * @param onError               OnError action to call
     * @param onCompleted           OnCompleted action to call
     * @param withDefaultSchedulers True if default schedulers should be applied.
     * @param <Result>              Result type of the flowable.
     */
    @Suppress("UNCHECKED_CAST")
    fun <Result> start(
        tag: String,
        flowable: Flowable<Result>,
        onNext: OnNext<V, Result>,
        onError: OnError<V>,
        onCompleted: OnCompleted<V>? = null,
        withDefaultSchedulers: Boolean = true
    ) {
        var finalFlowable = flowable
        var cached = cache[tag] as? CacheableStream<V, Result>

        if (!cache.containsKey(tag)) {
            MVPLogger.d(classTag, "Starting task : $tag")
            if (withDefaultSchedulers) {
                finalFlowable = flowable.compose(applyFlowableIOScheduler())
            }
            cached = CacheableStream(
                finalFlowable,
                viewSubject,
                getCacheableStreamConsumer(tag, onNext, onError, onCompleted)
            )

            if (!cache.containsKey(tag)) {
                cache[tag] = cached
            } else {
                cached.cancel()
                cached = cache[tag] as? CacheableStream<V, Result>
            }
        } else {
            MVPLogger.d(classTag, "Resuming task : $tag")
        }

        cached?.resume()
    }

    @Suppress("UNCHECKED_CAST")
    fun <Result> start(
        tag: String,
        flowable: Flowable<Result>,
        onNext: (V, Result) -> Unit,
        onError: (V, Throwable) -> Unit,
        onCompleted: ((V) -> Unit)? = null,
        withDefaultSchedulers: Boolean = true
    ) {
        start(
            tag, flowable,
            object : OnNext<V, Result> {
                override fun accept(u: V, v: Result) {
                    onNext(u, v)
                }
            },
            object : OnError<V> {
                override fun accept(u: V, throwable: Throwable) {
                    onError(u, throwable)
                }
            },
            onCompleted?.let {
                object : OnCompleted<V> {
                    override fun accept(u: V) {
                        it(u)
                    }
                }
            },
            withDefaultSchedulers
        )
    }

    /**
     * Starts a [single].
     *
     * If an existing single with the same [tag] exists in cache, the single will be resumed.
     * Otherwise it will be added in the cache and started.
     *
     * The [withDefaultSchedulers] parameter is used to attach or not the single to default schedulers
     * using [observableIOSchedulerTransformer].
     *
     * @param tag                   Single tag.
     * @param single                Single to start.
     * @param onNext                OnNext action to call
     * @param onError               OnError action to call
     * @param onCompleted           OnCompleted action to call
     * @param withDefaultSchedulers True if default schedulers should be applied.
     * @param <Result>              Result type of the single.
     */
    @Suppress("UNCHECKED_CAST")
    fun <Result> start(
        tag: String,
        single: Single<Result>,
        onNext: OnNext<V, Result>,
        onError: OnError<V>,
        onCompleted: OnCompleted<V>? = null,
        withDefaultSchedulers: Boolean = true
    ) {
        var finalSingle = single
        var cached = cache[tag] as? CacheableStream<V, Result>

        if (!cache.containsKey(tag)) {
            MVPLogger.d(classTag, "Starting task : $tag")
            if (withDefaultSchedulers) {
                finalSingle = single.compose(applySingleIOScheduler())
            }
            cached = CacheableStream(
                finalSingle,
                viewSubject,
                getCacheableStreamConsumer(tag, onNext, onError, onCompleted)
            )

            if (!cache.containsKey(tag)) {
                cache[tag] = cached
            } else {
                cached.cancel()
                cached = cache[tag] as? CacheableStream<V, Result>
            }
        } else {
            MVPLogger.d(classTag, "Resuming task : $tag")
        }

        cached?.resume()
    }

    @Suppress("UNCHECKED_CAST")
    fun <Result> start(
        tag: String,
        single: Single<Result>,
        onNext: (V, Result) -> Unit,
        onError: (V, Throwable) -> Unit,
        onCompleted: ((V) -> Unit)? = null,
        withDefaultSchedulers: Boolean = true
    ) {
        start(
            tag, single,
            object : OnNext<V, Result> {
                override fun accept(u: V, v: Result) {
                    onNext(u, v)
                }
            },
            object : OnError<V> {
                override fun accept(u: V, throwable: Throwable) {
                    onError(u, throwable)
                }
            },
            onCompleted?.let {
                object : OnCompleted<V> {
                    override fun accept(u: V) {
                        it(u)
                    }
                }
            },
            withDefaultSchedulers
        )
    }

    /**
     * Starts a [completable].
     *
     * If an existing completable with the same [tag] exists in cache, the completable will be resumed.
     * Otherwise it will be added in the cached and started.
     *
     * The [withDefaultSchedulers] parameter is used to attach or not the completable to default schedulers
     * using [observableIOSchedulerTransformer].
     *
     * @param tag                   Completable tag.
     * @param completable           Completable to start.
     * @param onError               OnError action to call
     * @param onCompleted           OnCompleted action to call
     * @param withDefaultSchedulers True if default schedulers should be applied.
     */
    @Suppress("UNCHECKED_CAST")
    fun <Result> start(
        tag: String,
        completable: Completable,
        onError: OnError<V>,
        onCompleted: OnCompleted<V>? = null,
        withDefaultSchedulers: Boolean = true
    ) {
        var finalCompletable = completable
        var cached = cache[tag] as? CacheableStream<V, Result>

        if (!cache.containsKey(tag)) {
            MVPLogger.d(classTag, "Starting task : $tag")
            if (withDefaultSchedulers) {
                finalCompletable = completable.compose(applyCompletableIOScheduler())
            }
            cached = CacheableStream(
                finalCompletable,
                viewSubject,
                getCacheableStreamConsumer(tag, null, onError, onCompleted)
            )

            if (!cache.containsKey(tag)) {
                cache[tag] = cached
            } else {
                cached.cancel()
                cached = cache[tag] as? CacheableStream<V, Result>
            }
        } else {
            MVPLogger.d(classTag, "Resuming task : $tag")
        }

        cached?.resume()
    }

    @Suppress("UNCHECKED_CAST")
    fun start(
        tag: String,
        completable: Completable,
        onError: (V, Throwable) -> Unit,
        onCompleted: ((V) -> Unit)? = null,
        withDefaultSchedulers: Boolean = true
    ) {
        start<Unit>(
            tag, completable,
            object : OnError<V> {
                override fun accept(u: V, throwable: Throwable) {
                    onError(u, throwable)
                }
            },
            onCompleted?.let {
                object : OnCompleted<V> {
                    override fun accept(u: V) {
                        it(u)
                    }
                }
            },
            withDefaultSchedulers
        )
    }

    /**
     * Starts a [maybe].
     *
     * If an existing maybe with the same [tag] exists in cache, the maybe will be resumed.
     * Otherwise it will be added in the cache and started.
     *
     * The [withDefaultSchedulers] parameter is used to attach or not the maybe to default schedulers
     * using [observableIOSchedulerTransformer].
     *
     * @param tag                   Maybe tag.
     * @param maybe                 Maybe to start.
     * @param onNext                OnNext action to call
     * @param onError               OnError action to call
     * @param onCompleted           OnCompleted action to call
     * @param withDefaultSchedulers True if default schedulers should be applied.
     * @param <Result>              Result type of the maybe.
     */
    @Suppress("UNCHECKED_CAST")
    fun <Result> start(
        tag: String,
        maybe: Maybe<Result>,
        onNext: OnNext<V, Result>,
        onError: OnError<V>,
        onCompleted: OnCompleted<V>? = null,
        withDefaultSchedulers: Boolean = true
    ) {
        var finalMaybe = maybe
        var cached = cache[tag] as? CacheableStream<V, Result>

        if (!cache.containsKey(tag)) {
            MVPLogger.d(classTag, "Starting task : $tag")
            if (withDefaultSchedulers) {
                finalMaybe = maybe.compose(applyMaybeIOScheduler())
            }
            cached = CacheableStream(
                finalMaybe,
                viewSubject,
                getCacheableStreamConsumer(tag, onNext, onError, onCompleted)
            )

            if (!cache.containsKey(tag)) {
                cache[tag] = cached
            } else {
                cached.cancel()
                cached = cache[tag] as? CacheableStream<V, Result>
            }
        } else {
            MVPLogger.d(classTag, "Resuming task : $tag")
        }

        cached?.resume()
    }

    @Suppress("UNCHECKED_CAST")
    fun <Result> start(
        tag: String,
        maybe: Maybe<Result>,
        onNext: (V, Result) -> Unit,
        onError: (V, Throwable) -> Unit,
        onCompleted: ((V) -> Unit)? = null,
        withDefaultSchedulers: Boolean = true
    ) {
        start(
            tag, maybe,
            object : OnNext<V, Result> {
                override fun accept(u: V, v: Result) {
                    onNext(u, v)
                }
            },
            object : OnError<V> {
                override fun accept(u: V, throwable: Throwable) {
                    onError(u, throwable)
                }
            },
            onCompleted?.let {
                object : OnCompleted<V> {
                    override fun accept(u: V) {
                        it(u)
                    }
                }
            },
            withDefaultSchedulers
        )
    }
}