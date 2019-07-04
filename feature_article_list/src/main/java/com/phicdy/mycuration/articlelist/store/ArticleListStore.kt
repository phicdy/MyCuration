package com.phicdy.mycuration.articlelist.store

import com.phicdy.mycuration.articlelist.action.FetchArticleAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import com.phicdy.mycuration.entity.Article

class ArticleListStore(
        dispatcher: Dispatcher
) : Store<List<Article>>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is FetchArticleAction -> _state.value = action.value
        }
    }
}