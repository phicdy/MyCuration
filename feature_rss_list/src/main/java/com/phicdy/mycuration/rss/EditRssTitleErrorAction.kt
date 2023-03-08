package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.Action

data class EditRssTitleErrorAction(override val value: RssListMessage.Type) : Action<RssListMessage.Type>
