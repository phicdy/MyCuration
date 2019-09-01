package com.phicdy.mycuration.rss

import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.coroutineScope
import java.util.ArrayList

class RssListPresenter(private val view: RssListView,
                       private val preferenceHelper: PreferenceHelper,
                       private val rssRepository: RssRepository,
                       private val networkTaskManager: NetworkTaskManager) {

    var unreadOnlyFeeds = arrayListOf<Feed>()
        private set
    var allFeeds = arrayListOf<Feed>()
        private set

    // Manage hide feed status
    private var isHided = true

    private val isAfterInterval: Boolean
        get() = System.currentTimeMillis() - preferenceHelper.lastUpdateDate >= 1000 * 60

    fun create() {}

    suspend fun resume() = coroutineScope {
        if (rssRepository.getNumOfRss() == 0) {
            updateViewForEmpty()
        } else {
            view.showAllUnreadView()
            view.showRecyclerView()
            view.hideEmptyView()
            fetchAllRss()
            refreshList()
            if (preferenceHelper.autoUpdateInMainUi && isAfterInterval) {
                view.setRefreshing(true)
                updateAllRss()
            }
        }
    }

    private fun generateHidedFeedList() {
        if (allFeeds.isEmpty()) return
        unreadOnlyFeeds = allFeeds.filter { it.unreadAriticlesCount > 0 } as ArrayList<Feed>
        if (unreadOnlyFeeds.isEmpty()) {
            unreadOnlyFeeds = allFeeds
        }
    }

    private fun refreshList() {
        generateHidedFeedList()
        if (isHided) {
            view.init(unreadOnlyFeeds.toRssListItem())
        } else {
            view.init(allFeeds.toRssListItem())
        }
        view.setTotalUnreadCount(allFeeds.sumBy { it.unreadAriticlesCount })
    }

    fun pause() {}

    fun updateFeedTitle(feedId: Int, newTitle: String) {
        for (feed in allFeeds) {
            if (feed.id == feedId) {
                feed.title = newTitle
                break
            }
        }
        for (feed in unreadOnlyFeeds) {
            if (feed.id == feedId) {
                feed.title = newTitle
                break
            }
        }
        view.notifyDataSetChanged(if (isHided) unreadOnlyFeeds.toRssListItem() else allFeeds.toRssListItem())
    }

    suspend fun deleteFeedAtPosition(position: Int) = coroutineScope {
        fun deleteAtPosition(currentList: ArrayList<Feed>, oppositeList: ArrayList<Feed>) {
            if (currentList.size <= position) return
            val (id) = currentList[position]
            currentList.removeAt(position)
            for (i in oppositeList.indices) {
                if (oppositeList[i].id == id) {
                    oppositeList.removeAt(i)
                    break
                }
            }
        }

        if (isHided) {
            deleteAtPosition(unreadOnlyFeeds, allFeeds)
        } else {
            deleteAtPosition(allFeeds, unreadOnlyFeeds)
        }
        refreshList()
        if (allFeeds.isEmpty()) updateViewForEmpty()
    }

    private fun updateViewForEmpty() {
        view.hideAllUnreadView()
        view.hideRecyclerView()
        view.showEmptyView()
    }

    fun onRssFooterClicked() {
        changeHideStatus()
    }

    private fun changeHideStatus() {
        generateHidedFeedList()
        if (isHided) {
            isHided = false
            view.init(allFeeds.toRssListItem())
        } else {
            isHided = true
            view.init(unreadOnlyFeeds.toRssListItem())
        }
    }

    suspend fun onRefresh() = coroutineScope {
        if (allFeeds.isEmpty()) {
            onRefreshComplete()
            return@coroutineScope
        }
        updateAllRss()
    }

    private suspend fun updateAllRss() = coroutineScope {
        try {
            networkTaskManager.updateAll(allFeeds)
            onFinishUpdate()
        } catch (e: Exception) {
        }
    }

    private fun onRefreshComplete() {
        view.onRefreshCompleted()
    }

    suspend fun onFinishUpdate() = coroutineScope {
        fetchAllRss()
        refreshList()
        onRefreshComplete()
        preferenceHelper.lastUpdateDate = System.currentTimeMillis()
    }

    private suspend fun fetchAllRss() = coroutineScope {
        allFeeds = rssRepository.getAllFeedsWithNumOfUnreadArticles()
    }

    private fun ArrayList<Feed>.toRssListItem(): List<RssListItem> = mutableListOf<RssListItem>().apply {
        this@toRssListItem.map {
            this.add(RssListItem.Content(
                    rssId = it.id,
                    rssTitle = it.title,
                    isDefaultIcon = it.iconPath.isBlank() || it.iconPath == Feed.DEDAULT_ICON_PATH,
                    rssIconPath = it.iconPath,
                    unreadCount = it.unreadAriticlesCount
            ))
        }
        add(RssListItem.Footer(if (isHided) RssListFooterState.UNREAD_ONLY else RssListFooterState.ALL))
    }
}
