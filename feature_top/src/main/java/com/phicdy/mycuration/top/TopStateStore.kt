package com.phicdy.mycuration.top

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import javax.inject.Inject

class TopStateStore @Inject constructor(
    dispatcher: Dispatcher
): Store<TopState>(dispatcher) {
    override suspend fun notify(action: Action<*>) {
        when (action) {
            is InitializeTopAction -> _state.value = state.value?.copy(numOfRss = action.value.numOfRss)
        }
    }
}