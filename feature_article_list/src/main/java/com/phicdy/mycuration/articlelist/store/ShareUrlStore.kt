package com.phicdy.mycuration.articlelist.store

import androidx.hilt.lifecycle.ViewModelInject
import com.phicdy.mycuration.articlelist.action.ShareUrlAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class ShareUrlStore @ViewModelInject constructor(
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