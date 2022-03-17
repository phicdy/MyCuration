package com.phicdy.mycuration.top

import com.phicdy.mycuration.core.Action

internal data class InitializeTopAction(override val value: InitializeTopValue) : Action<InitializeTopValue>