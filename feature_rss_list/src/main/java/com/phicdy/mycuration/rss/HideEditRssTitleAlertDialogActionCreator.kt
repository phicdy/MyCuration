package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import javax.inject.Inject

class HideEditRssTitleAlertDialogActionCreator @Inject constructor(
    private val dispatcher: Dispatcher
) : ActionCreator {

    override suspend fun run() {
        dispatcher.dispatch(HideEditRssTitleAlertDialogAction(Unit))
    }
}