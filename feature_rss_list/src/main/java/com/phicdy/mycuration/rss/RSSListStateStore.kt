package com.phicdy.mycuration.rss

import androidx.hilt.lifecycle.ViewModelInject
import com.phicdy.action.articlelist.ReadAllArticlesAction
import com.phicdy.action.articlelist.ReadArticleAction
import com.phicdy.action.articlelist.UnReadArticleAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import com.phicdy.mycuration.entity.RssListMode

class RSSListStateStore @ViewModelInject constructor(
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
                    if (state !is RssListState.Loaded) return
                    val updated = state.rawRssList.map {
                        if (it.id == action.value.rssId) {
                            it.copy(unreadAriticlesCount = it.unreadAriticlesCount - action.value.count)
                        } else {
                            it
                        }
                    }
                    val (mode, item) = rssListItemFactory.create(state.mode, updated)
                    _state.value = RssListState.Loaded(item, updated, mode)
                }
            }
            is UnReadArticleAction -> {
                state.value?.let { state ->
                    if (state !is RssListState.Loaded) return
                    val updated = state.rawRssList.map {
                        if (it.id == action.value.rssId) {
                            it.copy(unreadAriticlesCount = it.unreadAriticlesCount + action.value.count)
                        } else {
                            it
                        }
                    }
                    val (mode, item) = rssListItemFactory.create(state.mode, updated)
                    _state.value = RssListState.Loaded(item, updated, mode)
                }
            }
            is ReadAllArticlesAction -> {
                state.value?.let { state ->
                    if (state !is RssListState.Loaded) return
                    val updated = state.rawRssList.map {
                        if (it.id == action.value.rssId) {
                            it.copy(unreadAriticlesCount = 0)
                        } else {
                            it
                        }
                    }
                    val (mode, item) = rssListItemFactory.create(state.mode, updated)
                    _state.value = RssListState.Loaded(item, updated, mode)
                }
            }
            is RssListUpdateAction -> {
                state.value?.let { state ->
                    when (val value = action.value) {
                        is RssListUpdateState.Started -> {
                            if (state !is RssListState.Loaded) return
                            val loaded = state.copy()
                            _state.value = RssListState.StartPullToRefresh
                            _state.value = loaded
                        }
                        is RssListUpdateState.Finished -> {
                            if (state !is RssListState.Loaded) return
                            _state.value = RssListState.FinishPullToRefresh
                            val (mode, item) = rssListItemFactory.create(RssListMode.UNREAD_ONLY, value.updated)
                            _state.value = RssListState.Loaded(item, value.updated, mode)
                        }
                        is RssListUpdateState.Failed -> {
                            if (state !is RssListState.Loaded) return
                            val loaded = state.copy()
                            _state.value = RssListState.FinishPullToRefresh
                            _state.value = loaded
                        }
                    }
                }
            }
        }
    }
}