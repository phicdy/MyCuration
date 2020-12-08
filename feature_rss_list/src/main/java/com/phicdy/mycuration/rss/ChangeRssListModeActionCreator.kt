package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.entity.RssListMode
import javax.inject.Inject

class ChangeRssListModeActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val rssListItemFactory: RssListItemFactory
) : ActionCreator1<RssListState.Loaded> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(state: RssListState.Loaded) {
        when (state.mode) {
            RssListMode.UNREAD_ONLY -> {
                val (_, item) = rssListItemFactory.create(RssListMode.ALL, state.rawRssList)
                RssListState.Loaded(
                        item = item,
                        mode = RssListMode.ALL,
                        rawRssList = state.rawRssList
                )
            }
            RssListMode.ALL -> {
                val (_, item) = rssListItemFactory.create(RssListMode.UNREAD_ONLY, state.rawRssList)
                RssListState.Loaded(
                        item = item,
                        mode = RssListMode.UNREAD_ONLY,
                        rawRssList = state.rawRssList
                )
            }
        }.let {
            dispatcher.dispatch(RssListAction(it))
        }
    }
}