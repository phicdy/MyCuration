package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.Action

data class ConsumeRssListMessageAction(override val value: RssListMessage) : Action<RssListMessage>
