package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import javax.inject.Inject

class ShowDeleteRssAlertDialogActionCreator @Inject constructor(
    private val dispatcher: Dispatcher
) : ActionCreator1<Int> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(rssId: Int) {
        dispatcher.dispatch(ShowDeleteRssAlertDialogAction(rssId))
    }
}