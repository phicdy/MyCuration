package com.phicdy.mycuration.rss

import androidx.hilt.lifecycle.ViewModelInject
import com.phicdy.action.articlelist.ReadAllArticlesAction
import com.phicdy.action.articlelist.ReadArticleAction
import com.phicdy.action.articlelist.UnReadArticleAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store

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
                    _state.value = RssListState.Loaded(rssListItemFactory.create(state.mode, updated), updated, state.mode)
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
                    _state.value = RssListState.Loaded(rssListItemFactory.create(state.mode, updated), updated, state.mode)
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
                    _state.value = RssListState.Loaded(rssListItemFactory.create(state.mode, updated), updated, state.mode)
                }
            }
            is RssListUpdateAction -> {
                state.value?.let { state ->
                    when (val value = action.value) {
                        is RssListUpdateState.Updating -> {
                            if (state !is RssListState.Loaded) return
                            val updated = state.rawRssList.map { rss ->
                                if (rss.id == value.updated.id) {
                                    rss.copy(unreadAriticlesCount = value.updated.unreadAriticlesCount)
                                } else {
                                    rss
                                }
                            }
                            _state.value = RssListState.Loaded(rssListItemFactory.create(state.mode, updated), updated, state.mode)
                        }
                        is RssListUpdateState.Started -> {
                            if (state !is RssListState.Loaded) return
                            val loaded = state.copy()
                            _state.value = RssListState.StartPullToRefresh
                            _state.value = loaded
                        }
                        is RssListUpdateState.Finished, RssListUpdateState.Failed -> {
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