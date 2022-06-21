package com.phicdy.mycuration.top

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TopStateStore @Inject constructor(
    dispatcher: Dispatcher
): Store<TopState>(dispatcher) {
    override suspend fun notify(action: Action<*>) {
        when (action) {
            is InitializeTopAction -> _state.value =
                state.value?.copy(numOfRss = action.value.numOfRss)
            is ShowRateDialogAction -> _state.value =
                state.value?.copy(showRateDialog = true)
            is CloseRateDialogAction -> _state.value =
                state.value?.copy(showRateDialog = false)
        }
    }
}