package com.phicdy.mycuration.curatedarticlelist.store

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import com.phicdy.mycuration.curatedarticlelist.action.ShareUrlAction
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class ShareUrlStore(
        dispatcher: Dispatcher,
        context: CoroutineContext = Dispatchers.Main
) : Store<String>(dispatcher, context) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is ShareUrlAction -> _state.value = action.value
        }
    }

}