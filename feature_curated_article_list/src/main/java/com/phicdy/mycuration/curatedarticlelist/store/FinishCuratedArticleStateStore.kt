package com.phicdy.mycuration.curatedarticlelist.store

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import com.phicdy.mycuration.curatedarticlelist.action.FinishAction
import kotlinx.coroutines.CoroutineScope

class FinishCuratedArticleStateStore(
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