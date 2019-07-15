package com.phicdy.mycuration.articlelist.store

import com.phicdy.mycuration.articlelist.action.SearchArticleAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import com.phicdy.mycuration.entity.Article

class SearchResultStore(
        dispatcher: Dispatcher
) : Store<List<Article>>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is SearchArticleAction -> _state.value = action.value
        }
    }
}