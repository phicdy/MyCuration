package com.phicdy.mycuration.articlelist.store

import com.phicdy.mycuration.articlelist.action.SwipeAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store

class SwipePositionStore(
        dispatcher: Dispatcher
) : Store<Int>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is SwipeAction -> _state.value = action.value
        }
    }
}