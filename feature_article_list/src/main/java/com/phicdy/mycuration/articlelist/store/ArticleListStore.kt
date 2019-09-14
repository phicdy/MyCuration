package com.phicdy.mycuration.articlelist.store

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.articlelist.action.FetchArticleAction
import com.phicdy.mycuration.articlelist.action.UpdateFavoriteAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store

class ArticleListStore(
        dispatcher: Dispatcher
) : Store<List<ArticleItem>>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is FetchArticleAction -> _state.value = action.value
            is UpdateFavoriteAction -> _state.value = action.value
        }
    }
}