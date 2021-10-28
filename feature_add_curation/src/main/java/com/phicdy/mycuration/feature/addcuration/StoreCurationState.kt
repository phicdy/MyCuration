package com.phicdy.mycuration.feature.addcuration

sealed class StoreCurationState {
    object Loading : StoreCurationState()
    object SucceedToAdd: StoreCurationState()
    object SucceedToEdit: StoreCurationState()
    object EmptyNameError: StoreCurationState()
    object EmptyWordError: StoreCurationState()
    object SameNameExitError: StoreCurationState()
}