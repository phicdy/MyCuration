package com.phicdy.mycuration.feature.addcuration

sealed class AddCurationState {
    object Loading : AddCurationState()
    data class Loaded(
        val name: String,
        val words: List<String>,
        val titleField: String,
        val wordField: String,
    ) : AddCurationState()
}