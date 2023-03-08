package com.phicdy.mycuration.top

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import javax.inject.Inject

class CloseRateDialogActionCreator @Inject constructor(
    private val dispatcher: Dispatcher
) : ActionCreator {
    override suspend fun run() {
        dispatcher.dispatch(CloseRateDialogAction(Unit))
    }
}