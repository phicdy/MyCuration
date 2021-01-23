package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.RssListMode
import javax.inject.Inject

class FetchAllRssListActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val rssRepository: RssRepository,
        private val rssListItemFactory: RssListItemFactory
) : ActionCreator1<RssListMode> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(rssListMode: RssListMode) {
        dispatcher.dispatch(RssListAction(RssListState.Initializing))
        val rss = rssRepository.getAllFeedsWithNumOfUnreadArticles()
        if (rss.isEmpty()) {
            dispatcher.dispatch(RssListAction(RssListState.Initialized(emptyList(), emptyList(), rssListMode)))
            return
        }
        val (mode, item) = rssListItemFactory.create(rssListMode, rss)
        dispatcher.dispatch(RssListAction(RssListState.Initialized(item, rss, mode)))
    }
}