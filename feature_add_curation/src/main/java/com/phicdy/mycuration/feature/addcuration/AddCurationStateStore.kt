package com.phicdy.mycuration.feature.addcuration

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddCurationStateStore @Inject constructor(
        dispatcher: Dispatcher,
) : Store<AddCurationState>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is InitializeAddCurationAction -> {
                _state.value = action.value
            }
        }
    }
}