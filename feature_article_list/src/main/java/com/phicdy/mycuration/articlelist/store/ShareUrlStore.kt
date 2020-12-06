package com.phicdy.mycuration.articlelist.store

import androidx.hilt.lifecycle.ViewModelInject
import com.phicdy.mycuration.articlelist.action.ShareUrlAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store

class ShareUrlStore @ViewModelInject constructor(
        dispatcher: Dispatcher
) : Store<String>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is ShareUrlAction -> _state.value = action.value
        }
    }

}