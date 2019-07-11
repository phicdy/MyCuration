package com.phicdy.mycuration.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class Store<T>(
        protected val dispatcher: Dispatcher,
        val context: CoroutineContext = Dispatchers.Main
) : ViewModel(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + context

    protected val _state = MutableLiveData<T>()
    val state: LiveData<T>
        get() = _state

    abstract suspend fun notify(action: Action<*>)

    override fun onCleared() {
        job.cancel()
        dispatcher.unregister(this)
        super.onCleared()
    }
}
