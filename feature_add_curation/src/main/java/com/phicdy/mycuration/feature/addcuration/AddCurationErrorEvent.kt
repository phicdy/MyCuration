package com.phicdy.mycuration.feature.addcuration

sealed class AddCurationErrorEvent {
    object Empty: AddCurationErrorEvent()
    object Duplicated: AddCurationErrorEvent()
}