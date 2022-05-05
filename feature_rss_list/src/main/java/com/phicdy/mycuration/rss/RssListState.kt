package com.phicdy.mycuration.rss

import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode

data class RssListState(
    val item: List<RssListItem>,
    val rawRssList: List<Feed>,
    val mode: RssListMode,
    val isInitializing: Boolean,
    val isRefreshing: Boolean,
    val messageList: List<RssListMessage> = emptyList(),
    val showDropdownMenuId: Int?,
    val showDeleteRssDialogId: Int?,
)
