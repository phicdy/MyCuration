package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator3
import com.phicdy.mycuration.core.Dispatcher
import javax.inject.Inject

class ChangeRssTitleActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val rssListItemFactory: RssListItemFactory
) : ActionCreator3<Int, String, RssListState.Loaded> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(rssId: Int, rssTitle: String, state: RssListState.Loaded) {
        val updated = state.rawRssList.map { rss ->
            if (rss.id == rssId) {
                rss.copy(title = rssTitle)
            } else {
                rss
            }
        }
        val (mode, item) = rssListItemFactory.create(state.mode, updated)
        dispatcher.dispatch(RssListAction(
                RssListState.Loaded(
                        item = item,
                        mode = mode,
                        rawRssList = updated
                )
        ))
    }
}