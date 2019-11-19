package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.RssListMode

class FetchAllRssListActionCreator(
        private val dispatcher: Dispatcher,
        private val rssRepository: RssRepository,
        private val rssListItemFactory: RssListItemFactory
) : ActionCreator1<RssListMode> {

    override suspend fun run(arg: RssListMode) {
        val rss = rssRepository.getAllFeedsWithNumOfUnreadArticles()
        if (rss.isEmpty()) {
            dispatcher.dispatch(RssListAction(RssListState(emptyList(), emptyList(), arg)))
            return
        }
        rssListItemFactory.create(arg, rss).let {
            dispatcher.dispatch(RssListAction(RssListState(it, rss, arg)))
        }
    }
}