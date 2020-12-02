package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator2
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.entity.RssListMode
import com.phicdy.mycuration.entity.RssUpdateIntervalCheckDate
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class UpdateAllRssActionCreator(
        private val dispatcher: Dispatcher,
        private val networkTaskManager: NetworkTaskManager,
        private val preferenceHelper: PreferenceHelper,
        private val rssListItemFactory: RssListItemFactory,
        private val rssRepository: RssRepository
) : ActionCreator2<RssListMode, RssUpdateIntervalCheckDate> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(rssListMode: RssListMode, rssUpdateIntervalCheckDate: RssUpdateIntervalCheckDate) {
        val isAfterInterval = rssUpdateIntervalCheckDate.toTime() - preferenceHelper.lastUpdateDate >= 1000 * 60
        if (!isAfterInterval || !preferenceHelper.autoUpdateInMainUi) return
        try {
            dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Started))
            val rssList = rssRepository.getAllFeedsWithNumOfUnreadArticles()
            coroutineScope {
                rssList.map { rss -> async { networkTaskManager.updateFeed(rss) } }
                        .map { deferred ->
                            //            networkTaskManager.updateAll(rss).collect { rss ->
                            val replaced = rssList.map { rss ->
                                if (rss.id == deferred.await().id) {
                                    deferred.await()
                                } else {
                                    rss
                                }
                            }
                            rssListItemFactory.create(rssListMode, replaced).let {
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