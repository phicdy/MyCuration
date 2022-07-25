package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.entity.RssUpdateIntervalCheckDate
import javax.inject.Inject

class LaunchUpdateAllRssActionCreator @Inject constructor(
    private val dispatcher: Dispatcher,
    private val networkTaskManager: NetworkTaskManager,
    private val preferenceHelper: PreferenceHelper,
    private val rssRepository: RssRepository
) : ActionCreator1<RssUpdateIntervalCheckDate> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(rssUpdateIntervalCheckDate: RssUpdateIntervalCheckDate) {
        val isAfterInterval = rssUpdateIntervalCheckDate.toTime() - preferenceHelper.lastUpdateDate >= 5000 * 60
        if (!preferenceHelper.autoUpdateInMainUi || !isAfterInterval) {
            return
        }
        UpdateAllRssActionCreator(dispatcher, networkTaskManager, preferenceHelper, rssRepository).run()
    }
}