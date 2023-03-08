package com.phicdy.mycuration.feature.addcuration

import com.phicdy.mycuration.core.Action

data class UpdateTextFieldAction(override val value: UpdateTextFieldEvent) : Action<UpdateTextFieldEvent>