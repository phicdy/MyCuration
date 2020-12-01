package com.phicdy.mycuration.rss

import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode

sealed class RssListState {
    object Loading : RssListState()
    data class Loaded(
            val item: List<RssListItem>,
            val rss: List<Feed>,
            val mode: RssListMode
    ) : RssListState()
}