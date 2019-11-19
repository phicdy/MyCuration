package com.phicdy.mycuration.rss

import com.phicdy.mycuration.entity.Feed
import java.util.ArrayList

class RssListPresenter(private val view: RssListView) {

    var unreadOnlyFeeds = arrayListOf<Feed>()
        private set
    var allFeeds = arrayListOf<Feed>()
        private set

    // Manage hide feed status
    private var isHided = true

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
    }

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

    fun removeRss(rssId: Int) {
        for (i in allFeeds.indices) {
            if (allFeeds[i].id == rssId) {
                allFeeds.removeAt(i)
                break
            }
        }
        refreshList()
        if (allFeeds.isEmpty()) updateViewForEmpty()
    }

    private fun updateViewForEmpty() {
        view.hideRecyclerView()
        view.showEmptyView()
    }

    private fun ArrayList<Feed>.toRssListItem(): List<RssListItem> = mutableListOf<RssListItem>().apply {
        add(RssListItem.All(this@toRssListItem.sumBy { it.unreadAriticlesCount }))
        add(RssListItem.Favroite)
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
