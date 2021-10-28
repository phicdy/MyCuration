package com.phicdy.mycuration.feature.addcuration

import com.phicdy.mycuration.core.Action

data class InitializeAddCurationAction(override val value: AddCurationState) : Action<AddCurationState>