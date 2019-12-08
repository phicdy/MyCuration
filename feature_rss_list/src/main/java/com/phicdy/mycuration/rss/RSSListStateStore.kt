package com.phicdy.mycuration.rss

import com.phicdy.action.articlelist.ReadAllArticlesAction
import com.phicdy.action.articlelist.ReadArticleAction
import com.phicdy.action.articlelist.UnReadArticleAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store

class RSSListStateStore(
        dispatcher: Dispatcher,
        private val rssListItemFactory: RssListItemFactory
) : Store<RssListState>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is RssListAction -> _state.value = action.value
            is ReadArticleAction -> {
                state.value?.let { state ->
                    val updated = state.rss.map {
                        if (it.id == action.value.rssId) {
                            it.copy(unreadAriticlesCount = it.unreadAriticlesCount - action.value.count)
                        } else {
                            it
                        }
                    }
                    _state.value = RssListState(rssListItemFactory.create(state.mode, updated), updated, state.mode)
                }
            }
            is UnReadArticleAction -> {
                state.value?.let { state ->
                    val updated = state.rss.map {
                        if (it.id == action.value.rssId) {
                            it.copy(unreadAriticlesCount = it.unreadAriticlesCount + action.value.count)
                        } else {
                            it
                        }
                    }
                    _state.value = RssListState(rssListItemFactory.create(state.mode, updated), updated, state.mode)
                }
            }
            is ReadAllArticlesAction -> {
                state.value?.let { state ->
                    val updated = state.rss.map {
                        if (it.id == action.value.rssId) {
                            it.copy(unreadAriticlesCount = 0)
                        } else {
                            it
                        }
                    }
                    _state.value = RssListState(rssListItemFactory.create(state.mode, updated), updated, state.mode)
                }
            }
        }
    }
}