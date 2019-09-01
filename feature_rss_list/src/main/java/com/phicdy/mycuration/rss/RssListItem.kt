package com.phicdy.mycuration.rss

sealed class RssListItem {
    data class Content(
            val rssId: Int,
            val rssTitle: String,
            val isDefaultIcon: Boolean,
            val rssIconPath: String,
            val unreadCount: Int
    ) : RssListItem()

    data class Footer(val state: RssListFooterState) : RssListItem()
}

enum class RssListFooterState {
    ALL,
    UNREAD_ONLY
}
