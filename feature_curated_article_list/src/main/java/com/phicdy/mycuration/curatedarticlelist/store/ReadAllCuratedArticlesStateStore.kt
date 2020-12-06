package com.phicdy.mycuration.curatedarticlelist.store

import androidx.hilt.lifecycle.ViewModelInject
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import com.phicdy.mycuration.curatedarticlelist.action.ReadALlArticlesAction

class ReadAllCuratedArticlesStateStore @ViewModelInject constructor(
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