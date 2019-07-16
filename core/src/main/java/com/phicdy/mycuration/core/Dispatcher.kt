package com.phicdy.mycuration.core

import kotlinx.coroutines.withContext

class Dispatcher {

    private val stores = mutableListOf<Store<*>>()

    suspend fun <T> dispatch(action: Action<T>) {
        for (i in 0 until stores.size) {
            // Maybe unregistered from other thread
            if (i >= stores.size) continue
            val store = stores[i]
            withContext(store.coroutineContext) {
                store.notify(action)
            }
        }
    }

    fun register(store: Store<*>) {
        stores.add(store)
    }

    fun unregister(store: Store<*>) {
        stores.remove(store)
    }
}