package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.Action

data class DeleteRssFailedAction(override val value: Unit) : Action<Unit>
