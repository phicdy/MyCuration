package com.phicdy.mycuration.articlelist.store

import androidx.hilt.lifecycle.ViewModelInject
import com.phicdy.mycuration.articlelist.action.ScrollAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store

class ScrollPositionStore @ViewModelInject constructor(
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