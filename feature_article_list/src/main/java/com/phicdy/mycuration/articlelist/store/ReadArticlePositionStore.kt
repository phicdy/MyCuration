package com.phicdy.mycuration.articlelist.store

import com.phicdy.mycuration.articlelist.action.ReadArticlePositionAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReadArticlePositionStore @Inject constructor(
        dispatcher: Dispatcher
) : Store<Int>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is ReadArticlePositionAction -> _state.value = action.value
        }
    }
}