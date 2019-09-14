package com.phicdy.mycuration.rss

sealed class RssListItem : Equatable {

    data class All(val unreadCount: Int) : RssListItem()

    object Favroite : RssListItem() {
        override fun equals(other: Any?): Boolean = other is Favroite
    }

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

/**
 * For suppress lint of DiffUtil.ItemCallback
 * https://stackoverflow.com/questions/55895359/lint-error-suspicious-equality-check-equals-is-not-implemented-in-object-dif
 */
interface Equatable {
    override fun equals(other: Any?): Boolean
}
