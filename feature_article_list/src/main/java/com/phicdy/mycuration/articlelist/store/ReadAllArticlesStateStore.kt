package com.phicdy.mycuration.articlelist.store

import com.phicdy.mycuration.articlelist.action.ReadALlArticlesAction
import com.phicdy.mycuration.articlelist.action.ReadArticleAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store

class ReadAllArticlesStateStore(
        dispatcher: Dispatcher
) : Store<Unit>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is ReadALlArticlesAction -> _state.value = action.value
        }
    }
}