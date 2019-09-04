package com.phicdy.mycuration.articlelist.store

import com.phicdy.mycuration.articlelist.action.OpenInternalBrowserAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store

class OpenInternalWebBrowserStateStore(
        dispatcher: Dispatcher
) : Store<String>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is OpenInternalBrowserAction -> _state.value = action.value
        }
    }

}