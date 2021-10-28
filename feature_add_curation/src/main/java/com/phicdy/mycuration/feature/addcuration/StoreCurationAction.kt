package com.phicdy.mycuration.feature.addcuration

import com.phicdy.mycuration.core.Action

data class StoreCurationAction(override val value: StoreCurationState) : Action<StoreCurationState>