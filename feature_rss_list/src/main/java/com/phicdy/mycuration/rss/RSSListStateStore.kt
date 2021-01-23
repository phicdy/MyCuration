package com.phicdy.mycuration.rss

import androidx.hilt.lifecycle.ViewModelInject
import com.phicdy.action.articlelist.ReadAllArticlesAction
import com.phicdy.action.articlelist.ReadArticleAction
import com.phicdy.action.articlelist.UnReadArticleAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode

class RSSListStateStore @ViewModelInject constructor(
        dispatcher: Dispatcher,
        private val rssListItemFactory: RssListItemFactory
) : Store<RssListState>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    private fun RssListState.getRawList(): List<Feed> {
        return when (this) {
            is RssListState.Updated -> rawRssList
            is RssListState.Initialized -> rawRssList
            else -> emptyList()
        }
    }

    private fun RssListState.getMode(): RssListMode {
        return when (this) {
            is RssListState.Updated -> mode
            is RssListState.Initialized -> mode
            else -> RssListMode.UNREAD_ONLY
        }
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is RssListAction -> {
                _state.value = action.value
            }
            is ReadArticleAction -> {
                state.value?.let { state ->
                    val rawRssList = state.getRawList()
                    if (rawRssList.isEmpty()) return
                    val updated = rawRssList.map {
                        if (it.id == action.value.rssId) {
                            it.copy(unreadAriticlesCount = it.unreadAriticlesCount - action.value.count)
                        } else {
                            it
                        }
                    }
                    val (mode, item) = rssListItemFactory.create(state.getMode(), updated)
                    _state.value = RssListState.Updated(item, updated, mode)
                }
            }
            is UnReadArticleAction -> {
                state.value?.let { state ->
                    val rawRssList = state.getRawList()
                    if (rawRssList.isEmpty()) return
                    val updated = rawRssList.map {
                        if (it.id == action.value.rssId) {
                            it.copy(unreadAriticlesCount = it.unreadAriticlesCount + action.value.count)
                        } else {
                            it
                        }
                    }
                    val (mode, item) = rssListItemFactory.create(state.getMode(), updated)
                    _state.value = RssListState.Updated(item, updated, mode)
                }
            }
            is ReadAllArticlesAction -> {
                state.value?.let { state ->
                    val rawRssList = state.getRawList()
                    if (rawRssList.isEmpty()) return
                    val updated = rawRssList.map {
                        if (it.id == action.value.rssId) {
                            it.copy(unreadAriticlesCount = 0)
                        } else {
                            it
                        }
                    }
                    val (mode, item) = rssListItemFactory.create(state.getMode(), updated)
                    _state.value = RssListState.Updated(item, updated, mode)
                }
            }
            is RssListUpdateAction -> {
                when (val value = action.value) {
                    is RssListUpdateState.Started -> {
                        _state.value = RssListState.StartUpdate
                    }
                    is RssListUpdateState.Finished -> {
                        val (mode, item) = rssListItemFactory.create(RssListMode.UNREAD_ONLY, value.updated)
                        _state.value = RssListState.Updated(item, value.updated, mode)
                    }
                    is RssListUpdateState.Failed -> {
                        _state.value = RssListState.FailedToUpdate
                    }
                }
            }
        }
    }
}