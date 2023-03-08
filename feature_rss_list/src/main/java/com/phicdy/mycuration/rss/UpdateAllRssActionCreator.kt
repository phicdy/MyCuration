package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator2
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class UpdateAllRssActionCreator(
        private val dispatcher: Dispatcher,
        private val networkTaskManager: NetworkTaskManager,
        private val preferenceHelper: PreferenceHelper,
        private val rssListItemFactory: RssListItemFactory
) : ActionCreator2<List<Feed>, RssListMode> {

    override suspend fun run(arg1: List<Feed>, arg2: RssListMode) {
        try {
            dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Started))
            coroutineScope {
                arg1.map { async { networkTaskManager.updateFeed(it) } }
                        .map { rss ->
                            //            networkTaskManager.updateAll(arg1).collect { rss ->
                            val replaced = arg1.map {
                                if (it.id == rss.await().id) {
                                    rss.await()
                                } else {
                                    it
                                }
                            }
                            rssListItemFactory.create(arg2, replaced).let {
                                dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Updating(it)))
                            }
                        }
            }
            preferenceHelper.lastUpdateDate = System.currentTimeMillis()
            dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Finished))
        } catch (e: Exception) {
            dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Failed))
        }
    }
}