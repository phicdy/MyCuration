package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.entity.RssListMode
import javax.inject.Inject

class UpdateAllRssActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val networkTaskManager: NetworkTaskManager,
        private val preferenceHelper: PreferenceHelper,
        private val rssRepository: RssRepository
) : ActionCreator1<RssListMode> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(rssListMode: RssListMode) {
        try {
            dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Started))
            val rssList = rssRepository.getAllFeedsWithNumOfUnreadArticles()
            rssList.map { rss ->
                val updated = networkTaskManager.updateFeed(rss)
                dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Updating(updated)))
            }
            preferenceHelper.lastUpdateDate = System.currentTimeMillis()
            dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Finished))
        } catch (e: Exception) {
            dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Failed))
        }
    }
}