package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.domain.rss.UnreadCountManager
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.presentation.view.RssItemView
import com.phicdy.mycuration.presentation.view.RssListView
import com.phicdy.mycuration.presentation.view.fragment.RssListFragment
import com.phicdy.mycuration.util.PreferenceHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.experimental.coroutineScope
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.ArrayList

class RssListPresenter(private val view: RssListView,
                       private val preferenceHelper: PreferenceHelper,
                       private val dbAdapter: DatabaseAdapter,
                       private val rssRepository: RssRepository,
                       private val networkTaskManager: NetworkTaskManager,
                       private val unreadCountManager: UnreadCountManager) {

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
            view.init(unreadOnlyFeeds)
        } else {
            view.init(allFeeds)
        }
        updateAllUnreadArticlesCount()
    }

    private fun updateAllUnreadArticlesCount() {
        view.setTotalUnreadCount(unreadCountManager.total)
    }

    fun pause() {}

    fun onDeleteFeedMenuClicked(position: Int) {
        view.showDeleteFeedAlertDialog(position)
    }

    fun onEditFeedMenuClicked(position: Int) {
        view.showEditTitleDialog(position, getFeedTitleAtPosition(position))
    }

    private fun getFeedTitleAtPosition(position: Int): String {
        if (position < 0) return ""
        return if (isHided) {
            if (position > unreadOnlyFeeds.size - 1) return ""
            unreadOnlyFeeds[position].title
        } else {
            if (position > allFeeds.size - 1) return ""
            allFeeds[position].title
        }
    }

    suspend fun onEditFeedOkButtonClicked(newTitle: String, position: Int) = coroutineScope {
        if (newTitle.isBlank()) {
            view.showEditFeedTitleEmptyErrorToast()
        } else {
            val updatedFeedId = getFeedIdAtPosition(position)
            val numOfUpdate = rssRepository.saveNewTitle(updatedFeedId, newTitle)
            if (numOfUpdate == 1) {
                view.showEditFeedSuccessToast()
                updateFeedTitle(updatedFeedId, newTitle)
            } else {
                view.showEditFeedFailToast()
            }
        }
    }

    private fun updateFeedTitle(feedId: Int, newTitle: String) {
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
        view.notifyDataSetChanged()
    }

    fun onDeleteOkButtonClicked(position: Int) {
        if (dbAdapter.deleteFeed(getFeedIdAtPosition(position))) {
            deleteFeedAtPosition(position)
            view.showDeleteSuccessToast()
        } else {
            view.showDeleteFailToast()
        }
    }

    private fun getFeedIdAtPosition(position: Int): Int {
        if (position < 0) return -1

        if (isHided) {
            return if (position > unreadOnlyFeeds.size - 1) {
                -1
            } else unreadOnlyFeeds[position].id
        } else {
            if (position > allFeeds.size - 1) {
                return -1
            }
        }
        return allFeeds[position].id
    }

    private fun deleteFeedAtPosition(position: Int) {
        fun deleteAtPosition(currentList: ArrayList<Feed>, oppositeList: ArrayList<Feed>) {
            if (currentList.size <= position) return
            val (id) = currentList[position]
            dbAdapter.deleteFeed(id)
            unreadCountManager.deleteFeed(id)
            currentList.removeAt(position)
            for (i in oppositeList.indices) {
                if (oppositeList[i].id == id) {
                    oppositeList.removeAt(i)
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

    fun onRssItemClicked(position: Int, mListener: RssListFragment.OnFeedListFragmentListener?) {
        val feedId = getFeedIdAtPosition(position)
        if (feedId != -1) mListener?.onListClicked(feedId)
    }

    fun onRssFooterClicked() {
        changeHideStatus()
    }

    private fun changeHideStatus() {
        generateHidedFeedList()
        if (isHided) {
            isHided = false
            view.init(allFeeds)
        } else {
            isHided = true
            view.init(unreadOnlyFeeds)
        }
    }

    fun onRefresh() {
        if (allFeeds.isEmpty()) {
            onRefreshComplete()
            return
        }
        updateAllRss()
    }

    private fun updateAllRss() {
        networkTaskManager.updateAllFeeds(allFeeds)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<Feed> {
                    override fun onSubscribe(s: Subscription) {
                        s.request((allFeeds.size).toLong())
                    }

                    override fun onNext(feed: Feed) {}

                    override fun onError(t: Throwable) {}

                    override fun onComplete() {
                        onFinishUpdate()
                    }
                })
    }

    private fun onRefreshComplete() {
        view.onRefreshCompleted()
    }

    fun onFinishUpdate() {
        onRefreshComplete()
        fetchAllRss()
        refreshList()
        preferenceHelper.lastUpdateDate = System.currentTimeMillis()
    }

    private fun fetchAllRss() {
        allFeeds = dbAdapter.allFeedsWithNumOfUnreadArticles
    }

    fun getItemCount(): Int {
        // Add +1 for the footer
        if (isHided) return unreadOnlyFeeds.size + 1
        return allFeeds.size + 1
    }

    fun onBindRssViewHolder(position: Int, view: RssItemView.Content) {
        val feed = if (isHided) unreadOnlyFeeds[position] else allFeeds[position]
        if (feed.iconPath == Feed.DEDAULT_ICON_PATH) {
            view.showDefaultIcon()
        } else {
            if (!view.showIcon(feed.iconPath)) {
                dbAdapter.saveIconPath(feed.siteUrl, Feed.DEDAULT_ICON_PATH)
            }
        }
        view.updateTitle(feed.title)
        view.updateUnreadCount(
                if (isHided) unreadOnlyFeeds[position].unreadAriticlesCount.toString()
                else allFeeds[position].unreadAriticlesCount.toString()
        )
    }

    fun onBindRssFooterViewHolder(view: RssItemView.Footer) {
        if (isHided) {
            view.showAllView()
        } else {
            view.showHideView()
        }
    }

    fun onGetItemViewType(position: Int): Int {
        return if (isHided) {
            if (position == unreadOnlyFeeds.size) {
                RssListFragment.VIEW_TYPE_FOOTER
            } else {
                RssListFragment.VIEW_TYPE_RSS
            }
        } else {
            if (position == allFeeds.size) {
                RssListFragment.VIEW_TYPE_FOOTER
            } else {
                RssListFragment.VIEW_TYPE_RSS
            }
        }
    }
}
