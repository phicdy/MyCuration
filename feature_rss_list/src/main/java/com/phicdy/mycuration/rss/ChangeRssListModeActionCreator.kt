package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator2
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode
import javax.inject.Inject

class ChangeRssListModeActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val rssListItemFactory: RssListItemFactory
) : ActionCreator2<List<Feed>, RssListMode> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(rawRssList: List<Feed>, mode: RssListMode) {
        when (mode) {
            RssListMode.UNREAD_ONLY -> {
                val (_, item) = rssListItemFactory.create(RssListMode.ALL, rawRssList)
                RssListState(
                    item = item,
                    mode = RssListMode.ALL,
                    rawRssList = rawRssList,
                    isInitializing = false,
                    isRefreshing = false,
                    showDropdownMenuId = null,
                    showDeleteRssDialogId = null,
                    showEditRssTitleDialogId = null,
                    showEditRssTitleDialogTitle = null
                )
            }
            RssListMode.ALL -> {
                val (_, item) = rssListItemFactory.create(RssListMode.UNREAD_ONLY, rawRssList)
                RssListState(
                    item = item,
                    mode = RssListMode.UNREAD_ONLY,
                    rawRssList = rawRssList,
                    isInitializing = false,
                    isRefreshing = false,
                    showDropdownMenuId = null,
                    showDeleteRssDialogId = null,
                    showEditRssTitleDialogId = null,
                    showEditRssTitleDialogTitle = null
                )
            }
        }.let {
            dispatcher.dispatch(RssListAction(it))
        }
    }
}