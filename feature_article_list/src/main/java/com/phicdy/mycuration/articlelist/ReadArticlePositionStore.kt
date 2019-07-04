package com.phicdy.mycuration.articlelist

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store

class ReadArticlePositionStore(
        dispatcher: Dispatcher
) : Store<Int>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is ReadArticleAction -> _state.value = action.value
        }
    }
}