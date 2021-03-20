package com.phicdy.mycuration

import com.phicdy.mycuration.core.CoroutineDispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher

class TestCoroutineDispatcherProvider(private val testDispatcher: TestCoroutineDispatcher): CoroutineDispatcherProvider {
    override fun default(): CoroutineDispatcher = testDispatcher
    override fun io(): CoroutineDispatcher = testDispatcher
    override fun main(): CoroutineDispatcher = testDispatcher
    override fun unconfined(): CoroutineDispatcher = testDispatcher
}