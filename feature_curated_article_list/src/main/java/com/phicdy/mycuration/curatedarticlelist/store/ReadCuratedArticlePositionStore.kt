package com.phicdy.mycuration.curatedarticlelist.store

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import com.phicdy.mycuration.curatedarticlelist.action.ReadArticleAction

class ReadCuratedArticlePositionStore(
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