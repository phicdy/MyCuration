package com.phicdy.mycuration.core

import kotlinx.coroutines.withContext

class Dispatcher {

    private val stores = mutableListOf<Store>()

    suspend fun <T> dispatch(action: Action<T>) {
        stores.forEach {
            withContext(it.coroutineContext) {
                it.notify(action)
            }
        }
    }

    fun register(store: Store) {
        stores.add(store)
    }

    fun unregister(store: Store) {
        stores.remove(store)
    }
}