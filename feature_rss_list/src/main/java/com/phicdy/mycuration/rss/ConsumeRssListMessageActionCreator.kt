package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import javax.inject.Inject

class ConsumeRssListMessageActionCreator @Inject constructor(
    private val dispatcher: Dispatcher,
) : ActionCreator1<RssListMessage> {
    override suspend fun run(rssListMessage: RssListMessage) {
        dispatcher.dispatch(ConsumeRssListMessageAction(rssListMessage))
    }
}