package com.phicdy.mycuration.rss

import com.phicdy.mycuration.entity.Feed

sealed class RssListItem {
    data class Content(val rss: Feed) : RssListItem()
    object Footer : RssListItem()
}