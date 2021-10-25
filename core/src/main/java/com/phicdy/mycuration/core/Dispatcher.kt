package com.phicdy.mycuration.core

import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

class Dispatcher @Inject constructor() {

    private val stores = mutableListOf<Store<*>>()
    private val reducers = CopyOnWriteArrayList<Reducer>()

    suspend fun <T> dispatch(action: Action<T>) {
        for (i in 0 until stores.size) {
            // Maybe unregistered from other thread
            if (i >= stores.size) continue
            val store = stores[i]
            withContext(store.coroutineContext) {
                store.notify(action)
            }
        }
        for (i in 0 until reducers.size) {
            reducers[i].reduce(action)
        }
    }

    fun register(store: Store<*>) {
        if (stores.contains(store)) return
        stores.add(store)
    }

    fun unregister(store: Store<*>) {
        stores.remove(store)
    }

    fun register(reducer: Reducer) {
        reducers.add(reducer)
    }

    fun unregister(reducer: Reducer) {
        reducers.remove(reducer)
    }
}