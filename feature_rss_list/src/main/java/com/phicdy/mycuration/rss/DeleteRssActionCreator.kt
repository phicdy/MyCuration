package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator2
import com.phicdy.mycuration.core.Dispatcher

class DeleteRssActionCreator(
        private val dispatcher: Dispatcher,
        private val rssListItemFactory: RssListItemFactory
) : ActionCreator2<Int, RssListState.Loaded> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(rssId: Int, state: RssListState.Loaded) {
        val updated = state.rawRssList.filter { it.id != rssId }
        RssListState.Loaded(
                item = rssListItemFactory.create(state.mode, updated),
                mode = state.mode,
                rawRssList = updated
        ).let {
            dispatcher.dispatch(RssListAction(it))
        }
    }
}