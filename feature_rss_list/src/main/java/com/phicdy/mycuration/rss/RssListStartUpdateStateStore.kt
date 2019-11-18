package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store

class RssListStartUpdateStateStore(dispatcher: Dispatcher) : Store<RssListStartUpdateState>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is RssListStartUpdateAction -> _state.value = action.value
        }
    }
}