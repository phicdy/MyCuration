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
        dispatcher.dispatch(RssListAction(RssListState.Loading))
        val rss = rssRepository.getAllFeedsWithNumOfUnreadArticles()
        if (rss.isEmpty()) {
            dispatcher.dispatch(RssListAction(RssListState.Loaded(emptyList(), emptyList(), arg)))
            return
        }
        rssListItemFactory.create(arg, rss).let {
            dispatcher.dispatch(RssListAction(RssListState.Loaded(it, rss, arg)))
        }
    }
}