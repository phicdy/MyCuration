package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator4
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode
import javax.inject.Inject

class ChangeRssTitleActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val rssListItemFactory: RssListItemFactory
) : ActionCreator4<Int, String, List<Feed>, RssListMode> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(rssId: Int, rssTitle: String, rawRssList: List<Feed>, mode: RssListMode) {
        val updated = rawRssList.map { rss ->
            if (rss.id == rssId) {
                rss.copy(title = rssTitle)
            } else {
                rss
            }
        }
        val (newMode, item) = rssListItemFactory.create(mode, updated)
        dispatcher.dispatch(RssListAction(
                RssListState(
                    item = item,
                    mode = newMode,
                    rawRssList = updated,
                    isInitializing = false,
                    isRefreshing = false
                )
        ))
    }
}