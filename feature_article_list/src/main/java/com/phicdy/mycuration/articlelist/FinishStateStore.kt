package com.phicdy.mycuration.articlelist

import com.phicdy.mycuration.articlelist.action.FinishAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import kotlinx.coroutines.CoroutineScope

class FinishStateStore(
        dispatcher: Dispatcher
) : Store<Boolean>(dispatcher), CoroutineScope {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is FinishAction -> _state.value = true
        }
    }

}