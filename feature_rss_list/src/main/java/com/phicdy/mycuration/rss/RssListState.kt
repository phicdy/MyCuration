package com.phicdy.mycuration.rss

import com.phicdy.mycuration.entity.RssListMode

data class RssListState(
        val item: List<RssListItem>,
        val mode: RssListMode
)