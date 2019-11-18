package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store

class RSSListStateStore(dispatcher: Dispatcher) : Store<RssListState>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is RssListAction -> _state.value = action.value
        }
    }
}