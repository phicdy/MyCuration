package com.phicdy.mycuration.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class DefaultCoroutineDispatcherProvider : CoroutineDispatcherProvider {
        override fun main(): CoroutineDispatcher = Dispatchers.Main
        override fun default(): CoroutineDispatcher = Dispatchers.Default
        override fun io(): CoroutineDispatcher = Dispatchers.IO
        override fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined
}