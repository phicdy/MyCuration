package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import javax.inject.Inject

class NewRssTitleChangeActionCreator @Inject constructor(
    private val dispatcher: Dispatcher
) : ActionCreator1<String> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(title: String) {
        dispatcher.dispatch(NewRssTitleChangeAction(title))
    }
}