package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator2
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.RssListMode
import kotlinx.coroutines.flow.collect

class UpdateAllRssActionCreator(
        private val dispatcher: Dispatcher,
        private val networkTaskManager: NetworkTaskManager,
        private val preferenceHelper: PreferenceHelper
) : ActionCreator2<List<Feed>, RssListMode> {

    override suspend fun run(arg1: List<Feed>, arg2: RssListMode) {
        try {
            dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Started))
            networkTaskManager.updateAll(arg1).collect { rss ->
                val replaced = arg1.map {
                    if (it.id == rss.id) {
                        rss
                    } else {
                        it
                    }
                }
                val list = mutableListOf<RssListItem>().apply {
                    add(RssListItem.All(replaced.sumBy { it.unreadAriticlesCount }))
                    add(RssListItem.Favroite)
                    replaced.map {
                        this.add(RssListItem.Content(
                                rssId = it.id,
                                rssTitle = it.title,
                                isDefaultIcon = it.iconPath.isBlank() || it.iconPath == Feed.DEDAULT_ICON_PATH,
                                rssIconPath = it.iconPath,
                                unreadCount = it.unreadAriticlesCount
                        ))
                    }
                    add(RssListItem.Footer(if (arg2 == RssListMode.UNREAD_ONLY) RssListFooterState.UNREAD_ONLY else RssListFooterState.ALL))
                }
                dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Updating(list)))
            }
            preferenceHelper.lastUpdateDate = System.currentTimeMillis()
            dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Finished))
        } catch (e: Exception) {
            dispatcher.dispatch(RssListUpdateAction(RssListUpdateState.Failed))
        }
    }
}