package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.Action

data class EditRssTitleSuccessAction(override val value: EditRssTitleValue) : Action<EditRssTitleValue>
