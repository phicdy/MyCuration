package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.entity.RssListMode

class ChangeRssListModeActionCreator(
        private val dispatcher: Dispatcher,
        private val rssListItemFactory: RssListItemFactory
) : ActionCreator1<RssListState> {

    override suspend fun run(arg: RssListState) {
        when (arg.mode) {
            RssListMode.UNREAD_ONLY -> {
                RssListState(
                        item = rssListItemFactory.create(RssListMode.ALL, arg.rss),
                        mode = RssListMode.ALL,
                        rss = arg.rss
                )
            }
            RssListMode.ALL -> {
                RssListState(
                        item = rssListItemFactory.create(RssListMode.UNREAD_ONLY, arg.rss),
                        mode = RssListMode.UNREAD_ONLY,
                        rss = arg.rss
                )
            }
        }.let {
            dispatcher.dispatch(RssListAction(it))
        }
    }
}