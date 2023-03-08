package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator2
import com.phicdy.mycuration.core.Dispatcher
import javax.inject.Inject

class ShowEditRssTitleAlertDialogActionCreator @Inject constructor(
    private val dispatcher: Dispatcher
) : ActionCreator2<Int, String> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(rssId: Int, title: String) {
        dispatcher.dispatch(ShowEditRssTitleAlertDialogAction(rssId, title))
    }
}