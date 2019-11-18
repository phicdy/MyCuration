package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.entity.Feed

class UpdateAllRssActionCreator(
        private val dispatcher: Dispatcher,
        private val networkTaskManager: NetworkTaskManager,
        private val preferenceHelper: PreferenceHelper
) : ActionCreator1<List<Feed>> {

    override suspend fun run(arg: List<Feed>) {
        try {
            dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Updating))
            networkTaskManager.updateAll(arg)
            preferenceHelper.lastUpdateDate = System.currentTimeMillis()
            dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Success))
        } catch (e: Exception) {
            dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Failed))
        }
    }
}