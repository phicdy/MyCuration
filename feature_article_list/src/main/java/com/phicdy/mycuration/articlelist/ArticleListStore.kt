package com.phicdy.mycuration.articlelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class ArticleListStore(private val dispatcher: Dispatcher) : Store, CoroutineScope, ViewModel() {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val _list = MutableLiveData<List<Article>>()
    val list: LiveData<List<Article>>
        get() = _list


    fun onCreate() {
        dispatcher.register(this)
    }

    override suspend fun <T> notify(action: Action<T>) {
        when (action) {
            is FetchArticleAction -> _list.value = action.value
        }
    }

    override fun onCleared() {
        dispatcher.unregister(this)
        super.onCleared()
    }
}