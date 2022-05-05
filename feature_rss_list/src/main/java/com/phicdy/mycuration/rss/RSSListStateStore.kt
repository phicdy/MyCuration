package com.phicdy.mycuration.rss

import com.phicdy.action.articlelist.ReadAllArticlesAction
import com.phicdy.action.articlelist.ReadArticleAction
import com.phicdy.action.articlelist.UnReadArticleAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import com.phicdy.mycuration.entity.RssListMode
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RSSListStateStore @Inject constructor(
        dispatcher: Dispatcher,
        private val rssListItemFactory: RssListItemFactory
) : Store<RssListState>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is RssListAction -> {
                _state.value = action.value
            }
            is ReadArticleAction -> {
                state.value?.let { state ->
                    val rawRssList = state.rawRssList
                    if (rawRssList.isEmpty()) return
                    val updated = rawRssList.map {
                        if (it.id == action.value.rssId) {
                            it.copy(unreadAriticlesCount = it.unreadAriticlesCount - action.value.count)
                        } else {
                            it
                        }
                    }
                    val (mode, item) = rssListItemFactory.create(state.mode, updated)
                    _state.value = _state.value?.copy(
                        item = item,
                        rawRssList = updated,
                        mode = mode
                    )
                }
            }
            is UnReadArticleAction -> {
                state.value?.let { state ->
                    val rawRssList = state.rawRssList
                    if (rawRssList.isEmpty()) return
                    val updated = rawRssList.map {
                        if (it.id == action.value.rssId) {
                            it.copy(unreadAriticlesCount = it.unreadAriticlesCount + action.value.count)
                        } else {
                            it
                        }
                    }
                    val (mode, item) = rssListItemFactory.create(state.mode, updated)
                    _state.value = _state.value?.copy(
                        item = item,
                        rawRssList = updated,
                        mode = mode
                    )
                }
            }
            is ReadAllArticlesAction -> {
                state.value?.let { state ->
                    val rawRssList = state.rawRssList
                    if (rawRssList.isEmpty()) return
                    val updated = rawRssList.map {
                        if (it.id == action.value.rssId) {
                            it.copy(unreadAriticlesCount = 0)
                        } else {
                            it
                        }
                    }
                    val (mode, item) = rssListItemFactory.create(state.mode, updated)
                    _state.value = _state.value?.copy(
                        item = item,
                        rawRssList = updated,
                        mode = mode
                    )
                }
            }
            is RssListUpdateAction -> {
                when (val value = action.value) {
                    is RssListUpdateState.Started -> {
                        _state.value = _state.value?.copy(isRefreshing = true)
                    }
                    is RssListUpdateState.Finished -> {
                        val (mode, item) = rssListItemFactory.create(
                            RssListMode.UNREAD_ONLY,
                            value.updated
                        )
                        _state.value = _state.value?.copy(
                            item = item,
                            rawRssList = value.updated,
                            mode = mode,
                            isRefreshing = false
                        )
                    }
                    is RssListUpdateState.Failed -> {
                        _state.value = _state.value?.copy(isRefreshing = false)
                    }
                }
            }
            is EditRssTitleSuccessAction -> {
                appendToMessageList(RssListMessage.Type.SUCCEED_TO_EDIT_RSS)
            }
            is EditRssTitleErrorAction -> {
                appendToMessageList(action.value)
            }
            is DeleteRssAction -> {
                val current = _state.value ?: return
                val updated = current.rawRssList.filter { it.id != action.value }
                val (newMode, item) = rssListItemFactory.create(current.mode, updated)
                val messageList =
                    current.messageList + RssListMessage(
                        UUID.randomUUID().mostSignificantBits,
                        RssListMessage.Type.SUCCEED_TO_DELETE_RSS
                    )
                _state.value = _state.value?.copy(
                    mode = newMode,
                    item = item,
                    rawRssList = updated,
                    messageList = messageList
                )
            }
            is DeleteRssFailedAction -> {
                appendToMessageList(RssListMessage.Type.ERROR_DELETE_RSS)
            }
            is ConsumeRssListMessageAction -> {
                val current = _state.value ?: return
                val messageList = current.messageList.filterNot { it.id == action.value.id }
                _state.value = _state.value?.copy(messageList = messageList)
            }
            is ShowDropdownMenuAction -> {
                _state.value = _state.value?.copy(showDropdownMenuId = action.value)
            }
            is HideDropdownMenuAction -> {
                _state.value = _state.value?.copy(showDropdownMenuId = null)
            }
            is ShowDeleteRssAlertDialogAction -> {
                _state.value = _state.value?.copy(showDeleteRssDialogId = action.value)
            }
            is HideDeleteRssAlertDialogAction -> {
                _state.value = _state.value?.copy(showDeleteRssDialogId = null)
            }
        }
    }

    private fun appendToMessageList(type: RssListMessage.Type) {
        val current = _state.value ?: return
        val messageList =
            current.messageList + RssListMessage(
                UUID.randomUUID().mostSignificantBits,
                type
            )
        _state.value = _state.value?.copy(messageList = messageList)
    }
}