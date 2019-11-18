package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode

class FetchAllRssListActionCreator(
        private val dispatcher: Dispatcher,
        private val rssRepository: RssRepository
) : ActionCreator1<RssListMode> {

    override suspend fun run(arg: RssListMode) {
        val rss = rssRepository.getAllFeedsWithNumOfUnreadArticles()
        if (rss.isEmpty()) {
            dispatcher.dispatch(RssListAction(RssListState(emptyList(), emptyList(), arg)))
            return
        }
        val list = mutableListOf<RssListItem>().apply {
            add(RssListItem.All(rss.sumBy { it.unreadAriticlesCount }))
            add(RssListItem.Favroite)
            rss.map {
                this.add(RssListItem.Content(
                        rssId = it.id,
                        rssTitle = it.title,
                        isDefaultIcon = it.iconPath.isBlank() || it.iconPath == Feed.DEDAULT_ICON_PATH,
                        rssIconPath = it.iconPath,
                        unreadCount = it.unreadAriticlesCount
                ))
            }
            add(RssListItem.Footer(if (arg == RssListMode.UNREAD_ONLY) RssListFooterState.UNREAD_ONLY else RssListFooterState.ALL))
        }
        dispatcher.dispatch(RssListAction(RssListState(list, rss, arg)))
    }
}