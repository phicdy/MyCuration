package com.phicdy.mycuration.articlelist.store

import com.phicdy.mycuration.articlelist.action.FinishAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@HiltViewModel
class FinishStateStore @Inject constructor(
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