package com.phicdy.mycuration.rss

import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode
import javax.inject.Inject

class RssListItemFactory @Inject constructor() {

    fun create(mode: RssListMode, allRss: List<Feed>): Pair<RssListMode, List<RssListItem>> {
        val newMode = when (mode) {
            RssListMode.UNREAD_ONLY -> if (allRss.none { it.unreadAriticlesCount > 0 }) {
                RssListMode.ALL
            } else {
                RssListMode.UNREAD_ONLY
            }
            RssListMode.ALL -> RssListMode.ALL
        }
        val list = mutableListOf<RssListItem>().apply {
            add(RssListItem.All(allRss.sumBy { it.unreadAriticlesCount }))
            add(RssListItem.Favroite)
            when (newMode) {
                RssListMode.UNREAD_ONLY -> allRss.filter { it.unreadAriticlesCount > 0 }
                RssListMode.ALL -> allRss
            }.map {
                this.add(RssListItem.Content(
                        rssId = it.id,
                        rssTitle = it.title,
                        isDefaultIcon = it.iconPath.isBlank() || it.iconPath == Feed.DEDAULT_ICON_PATH,
                        rssIconPath = it.iconPath,
                        unreadCount = it.unreadAriticlesCount
                ))
            }
            when (newMode) {
                RssListMode.UNREAD_ONLY -> RssListFooterState.UNREAD_ONLY
                RssListMode.ALL -> RssListFooterState.ALL
            }.let {
                add(RssListItem.Footer(it))
            }
        }
        return Pair(newMode, list)
    }
}