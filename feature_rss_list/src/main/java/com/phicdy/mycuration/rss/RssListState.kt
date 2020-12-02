package com.phicdy.mycuration.rss

import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode

sealed class RssListState {
    object Initializing : RssListState()
    object StartPullToRefresh : RssListState()
    object FinishPullToRefresh : RssListState()

    data class Loaded(
            val item: List<RssListItem>,
            val rawRssList: List<Feed>,
            val mode: RssListMode
    ) : RssListState()
}