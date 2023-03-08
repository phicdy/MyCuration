package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.Action

data class NewRssTitleChangeAction (override val value: String) : Action<String>
