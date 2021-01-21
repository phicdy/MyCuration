package com.phicdy.mycuration.rss

import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode

sealed class RssListState {
    object Initializing : RssListState()

    data class Initialized(
            val item: List<RssListItem>,
            val rawRssList: List<Feed>,
            val mode: RssListMode
    ) : RssListState()

    object StartUpdate : RssListState()
    object FailedToUpdate : RssListState()

    data class Updated(
            val item: List<RssListItem>,
            val rawRssList: List<Feed>,
            val mode: RssListMode
    ) : RssListState()
}