package com.phicdy.mycuration.feature.addcuration

import com.phicdy.mycuration.core.Action

data class AddCurationWordAction(override val value: String) : Action<String>