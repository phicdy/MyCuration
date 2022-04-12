package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator3
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode
import javax.inject.Inject

class DeleteRssActionCreator @Inject constructor(
    private val dispatcher: Dispatcher,
    private val rssRepository: RssRepository
) : ActionCreator3<Int, List<Feed>, RssListMode> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(rssId: Int, rawRssList: List<Feed>, mode: RssListMode) {
        if (rssRepository.deleteRss(rssId)) {
            dispatcher.dispatch(DeleteRssAction(rssId))
        } else {
            dispatcher.dispatch(DeleteRssFailedAction(Unit))
        }
    }
}