package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator3
import com.phicdy.mycuration.core.Dispatcher

class ChangeRssTitleActionCreator(
        private val dispatcher: Dispatcher,
        private val rssListItemFactory: RssListItemFactory
) : ActionCreator3<Int, String, RssListState.Loaded> {

    override suspend fun run(arg1: Int, arg2: String, arg3: RssListState.Loaded) {
        val updated = arg3.rss.map {
            if (it.id == arg1) {
                it.copy(title = arg2)
            } else {
                it
            }
        }
        RssListState.Loaded(
                item = rssListItemFactory.create(arg3.mode, updated),
                mode = arg3.mode,
                rss = updated
        ).let {
            dispatcher.dispatch(RssListAction(it))
        }
    }
}