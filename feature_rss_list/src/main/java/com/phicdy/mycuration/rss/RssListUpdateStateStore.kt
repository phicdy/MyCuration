package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store

class RssListUpdateStateStore(dispatcher: Dispatcher) : Store<RssListUpdateState>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is RssListUpdateAction -> _state.value = action.value
        }
    }
}