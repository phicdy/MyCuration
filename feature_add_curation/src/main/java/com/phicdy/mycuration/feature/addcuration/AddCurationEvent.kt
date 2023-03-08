package com.phicdy.mycuration.feature.addcuration

sealed class AddCurationEvent {
    object Empty : AddCurationEvent()
    object Duplicated : AddCurationEvent()
}