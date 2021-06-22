package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.entity.RssListMode
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
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
            preferenceHelper.lastUpdateDate = System.currentTimeMillis()
            Timber.d("start update rss")
            val now = System.currentTimeMillis()
            val rssList = rssRepository.getAllFeeds()
            coroutineScope {
                val deferred = rssList.map { rss ->
                    async { networkTaskManager.updateFeed(rss) }
                }
                val result = deferred.awaitAll()
                dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Finished(result)))
                val time = System.currentTimeMillis() - now
                Timber.d("fnish update rss, time: $time millisec")
            }
        } catch (e: Exception) {
            dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Failed))
            Timber.d("fnish update rss, error")
        }
    }
}