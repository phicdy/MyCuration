package com.phicdy.mycuration.articlelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class ReadArticlePositionStore(private val dispatcher: Dispatcher) : Store, CoroutineScope, ViewModel() {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val _position = MutableLiveData<Int>()
    val position: LiveData<Int>
        get() = _position

    fun onCreate() {
        dispatcher.register(this)
    }

    override suspend fun <T> notify(action: Action<T>) {
        when (action) {
            is ReadArticleAction -> _position.value = action.value
        }
    }

    override fun onCleared() {
        dispatcher.unregister(this)
        super.onCleared()
    }
}