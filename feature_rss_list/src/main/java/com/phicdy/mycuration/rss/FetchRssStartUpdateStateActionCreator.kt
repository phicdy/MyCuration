package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.entity.RssUpdateIntervalCheckDate

class FetchRssStartUpdateStateActionCreator(
        private val dispatcher: Dispatcher,
        private val preferenceHelper: PreferenceHelper
) : ActionCreator1<RssUpdateIntervalCheckDate> {

    override suspend fun run(arg: RssUpdateIntervalCheckDate) {
        val isAfterInterval = arg.toTime() - preferenceHelper.lastUpdateDate >= 1000 * 60
        dispatcher.dispatch(RssListStartUpdateAction(RssListStartUpdateState(preferenceHelper.autoUpdateInMainUi && isAfterInterval)))
    }
}