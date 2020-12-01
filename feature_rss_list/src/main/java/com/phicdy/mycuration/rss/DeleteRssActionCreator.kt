package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator2
import com.phicdy.mycuration.core.Dispatcher

class DeleteRssActionCreator(
        private val dispatcher: Dispatcher,
        private val rssListItemFactory: RssListItemFactory
) : ActionCreator2<Int, RssListState.Loaded> {

    override suspend fun run(arg1: Int, arg2: RssListState.Loaded) {
        val updated = arg2.rss.filter { it.id != arg1 }
        RssListState.Loaded(
                item = rssListItemFactory.create(arg2.mode, updated),
                mode = arg2.mode,
                rss = updated
        ).let {
            dispatcher.dispatch(RssListAction(it))
        }
    }
}