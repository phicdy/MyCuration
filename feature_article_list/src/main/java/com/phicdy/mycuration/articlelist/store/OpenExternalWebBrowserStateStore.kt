package com.phicdy.mycuration.articlelist.store

import com.phicdy.mycuration.articlelist.action.OpenExternalBrowserAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store

class OpenExternalWebBrowserStateStore(
        dispatcher: Dispatcher
) : Store<String>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is OpenExternalBrowserAction -> _state.value = action.value
        }
    }

}