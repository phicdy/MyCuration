package com.phicdy.mycuration.feature.addcuration

sealed class AddCurationState {
    object Loading : AddCurationState()
    data class Loaded(val name: String, val words: List<String>) : AddCurationState()
    data class Deleted(val name: String, val words: List<String>, val deletedPosition: Int) : AddCurationState()
}