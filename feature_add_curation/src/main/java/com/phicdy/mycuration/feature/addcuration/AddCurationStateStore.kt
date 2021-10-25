package com.phicdy.mycuration.feature.addcuration

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class AddCurationStateStore @Inject constructor(
        dispatcher: Dispatcher,
) : Store<AddCurationState>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    private val _event = MutableSharedFlow<AddCurationEvent>()
    val event: SharedFlow<AddCurationEvent> = _event

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is InitializeAddCurationAction -> {
                _state.value = action.value
            }
            is AddCurationWordAction -> {
                if (action.value.isBlank()) {
                    _event.emit(AddCurationEvent.Empty)
                    return
                }
                val value = state.value
                if (value is AddCurationState.Loaded) {
                    if (value.words.contains(action.value)) {
                        _event.emit(AddCurationEvent.Duplicated)
                        return
                    }
                    val newList = ArrayList(value.words + action.value)
                    _state.value = value.copy(words = newList)
                    _event.emit(AddCurationEvent.ResetWordInput)
                }
            }
            is DeleteCurationWordAction -> {
                val value = state.value
                if (value is AddCurationState.Loaded) {
                    val newList = ArrayList(value.words)
                    newList.removeAt(action.value)
                    _state.value = value.copy(words = newList)
                }
            }
            is UpdateTextFieldAction -> {
                val value = state.value
                if (value is AddCurationState.Loaded)
                    when (action.value.type) {
                        AddCurationTextFieldType.TITLE -> {
                            _state.value = value.copy(titleField = action.value.value)
                        }
                        AddCurationTextFieldType.WORD -> {
                            _state.value = value.copy(wordField = action.value.value)
                        }
                    }
            }
        }
    }
}