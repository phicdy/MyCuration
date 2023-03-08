package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator2
import com.phicdy.mycuration.core.Dispatcher

class DeleteRssActionCreator(
        private val dispatcher: Dispatcher,
        private val rssListItemFactory: RssListItemFactory
) : ActionCreator2<Int, RssListState> {

    override suspend fun run(arg1: Int, arg2: RssListState) {
        val updated = arg2.rss.filter { it.id != arg1 }
        RssListState(
                item = rssListItemFactory.create(arg2.mode, updated),
                mode = arg2.mode,
                rss = updated
        ).let {
            dispatcher.dispatch(RssListAction(it))
        }
    }
}