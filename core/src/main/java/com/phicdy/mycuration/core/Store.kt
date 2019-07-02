package com.phicdy.mycuration.core

import kotlin.coroutines.CoroutineContext

interface Store {
    val coroutineContext: CoroutineContext
    suspend fun <T> notify(action: Action<T>)
}
