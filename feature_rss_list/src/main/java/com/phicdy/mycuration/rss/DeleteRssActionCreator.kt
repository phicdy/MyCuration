package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator3
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode
import javax.inject.Inject

class DeleteRssActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val rssListItemFactory: RssListItemFactory
) : ActionCreator3<Int, List<Feed>, RssListMode> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(rssId: Int, rawRssList: List<Feed>, mode: RssListMode) {
        val updated = rawRssList.filter { it.id != rssId }
        val (newMode, item) = rssListItemFactory.create(mode, updated)
        RssListState.Updated(
                item = item,
                mode = newMode,
                rawRssList = updated
        ).let {
            dispatcher.dispatch(RssListAction(it))
        }
    }
}