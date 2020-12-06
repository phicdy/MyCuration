package com.phicdy.mycuration.curatedarticlelist.store

import androidx.hilt.lifecycle.ViewModelInject
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import com.phicdy.mycuration.curatedarticlelist.action.ScrollAction

class ScrollCuratedArticlePositionStore @ViewModelInject constructor(
        dispatcher: Dispatcher
) : Store<Int>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is ScrollAction -> _state.value = action.value
        }
    }
}