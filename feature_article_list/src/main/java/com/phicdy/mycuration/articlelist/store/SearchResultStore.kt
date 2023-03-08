package com.phicdy.mycuration.articlelist.store

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.articlelist.action.SearchArticleAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchResultStore @Inject constructor(
        dispatcher: Dispatcher
) : Store<List<ArticleItem>>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is SearchArticleAction -> _state.value = action.value
        }
    }
}