package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.domain.rss.UnreadCountManager
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.presentation.view.RssListView
import com.phicdy.mycuration.presentation.view.fragment.RssListFragment
import com.phicdy.mycuration.util.PreferenceHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.ArrayList

class RssListPresenter(private val view: RssListView,
                       private val preferenceHelper: PreferenceHelper,
                       private val dbAdapter: DatabaseAdapter,
                       private val networkTaskManager: NetworkTaskManager,
                       private val unreadCountManager: UnreadCountManager) : Presenter {

    var feeds: ArrayList<Feed> = ArrayList()
        private set
    var allFeeds: ArrayList<Feed> = ArrayList()
        private set

    // Manage hide feed status
    private var isHided = true

    private val isAfterInterval: Boolean
        get() = System.currentTimeMillis() - preferenceHelper.lastUpdateDate >= 1000 * 60

    override fun create() {}

    override fun resume() {
        allFeeds = dbAdapter.allFeedsWithNumOfUnreadArticles
        // For show/hide
        if (allFeeds.isNotEmpty()) {
            addShowHideLine(allFeeds)
            view.showAllUnreadView()
        }
        refreshList()
        if (networkTaskManager.isUpdatingFeed) {
            view.setRefreshing(true)
        }
        if (allFeeds.isNotEmpty() && preferenceHelper.autoUpdateInMainUi &&
                isAfterInterval && !networkTaskManager.isUpdatingFeed) {
            view.setRefreshing(true)
            networkTaskManager.updateAllFeeds(allFeeds)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Subscriber<Feed> {
                        override fun onSubscribe(s: Subscription) {
                            // Skip hide line
                            s.request((allFeeds.size - 1).toLong())
                        }

                        override fun onNext(feed: Feed) {}

                        override fun onError(t: Throwable) {

                        }

                        override fun onComplete() {
                            onFinishUpdate()
                        }
                    })
        }
    }

    private fun addShowHideLine(feeds: ArrayList<Feed>) {
        feeds.add(Feed(Feed.DEFAULT_FEED_ID, "", "", Feed.DEDAULT_ICON_PATH, "", 0, ""))
    }

    private fun generateHidedFeedList() {
        if (allFeeds.isEmpty()) return
        feeds = allFeeds.filter { it.unreadAriticlesCount > 0 } as ArrayList<Feed>
        if (feeds.isEmpty()) {
            feeds = allFeeds
        } else {
            addShowHideLine(feeds)
        }
    }

    private fun refreshList() {
        generateHidedFeedList()
        if (isHided) {
            view.init(feeds)
        } else {
            view.init(allFeeds)
        }
        updateAllUnreadArticlesCount()
    }

    private fun updateAllUnreadArticlesCount() {
        view.setTotalUnreadCount(unreadCountManager.total)
    }

    override fun pause() {
        if (networkTaskManager.isUpdatingFeed) {
            view.onRefreshCompleted()
        }
    }

    fun onDeleteFeedMenuClicked(position: Int) {
        view.showDeleteFeedAlertDialog(position)
    }

    fun onEditFeedMenuClicked(position: Int) {
        view.showEditTitleDialog(position, getFeedTitleAtPosition(position))
    }

    private fun getFeedTitleAtPosition(position: Int): String {
        if (position < 0 || position > feeds.size - 1) {
            return ""
        }
        return if (isHided) {
            feeds[position].title
        } else {
            allFeeds[position].title
        }
    }

    fun onEditFeedOkButtonClicked(newTitle: String, position: Int) {
        if (newTitle.isBlank()) {
            view.showEditFeedTitleEmptyErrorToast()
        } else {
            val updatedFeedId = getFeedIdAtPosition(position)
            val numOfUpdate = dbAdapter.saveNewTitle(updatedFeedId, newTitle)
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
        for (feed in feeds) {
            if (feed.id == feedId) {
                feed.title = newTitle
                break
            }
        }
        view.notifyDataSetChanged()
    }

    fun onDeleteOkButtonClicked(position: Int) {
        if (dbAdapter.deleteFeed(getFeedIdAtPosition(position))) {
            removeFeedAtPosition(position)
            view.showDeleteSuccessToast()
        } else {
            view.showDeleteFailToast()
        }
    }

    private fun getFeedIdAtPosition(position: Int): Int {
        if (position < 0) return -1

        if (isHided) {
            return if (position > feeds.size - 1) {
                -1
            } else feeds[position].id
        } else {
            if (position > allFeeds.size - 1) {
                return -1
            }
        }
        return allFeeds[position].id
    }

    private fun removeFeedAtPosition(position: Int) {
        if (isHided) {
            val (id) = feeds[position]
            dbAdapter.deleteFeed(id)
            unreadCountManager.deleteFeed(id)
            feeds.removeAt(position)
            for (i in allFeeds.indices) {
                if (allFeeds[i].id == id) {
                    allFeeds.removeAt(i)
                }
            }
        } else {
            val (id) = allFeeds[position]
            dbAdapter.deleteFeed(id)
            unreadCountManager.deleteFeed(id)
            allFeeds.removeAt(position)
            for (i in feeds.indices) {
                if (feeds[i].id == id) {
                    feeds.removeAt(i)
                }
            }
        }
        refreshList()
    }

    fun onFeedListClicked(position: Int, mListener: RssListFragment.OnFeedListFragmentListener?) {
        val feedId = getFeedIdAtPosition(position)
        if (feedId == Feed.DEFAULT_FEED_ID) {
            changeHideStatus()
            return
        }
        mListener?.onListClicked(feedId)
    }

    private fun changeHideStatus() {
        if (isHided) {
            isHided = false
            view.init(allFeeds)
        } else {
            isHided = true
            view.init(feeds)
        }
    }

    fun onRefresh() {
        if (allFeeds.isEmpty()) {
            onRefreshComplete()
            return
        }

        networkTaskManager.updateAllFeeds(allFeeds)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<Feed> {
                    override fun onSubscribe(s: Subscription) {
                        s.request((allFeeds.size - 1).toLong())
                    }

                    override fun onNext(feed: Feed) {}

                    override fun onError(t: Throwable) {

                    }

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
        refreshList()
        preferenceHelper.lastUpdateDate = System.currentTimeMillis()
    }

    fun activityCreated() {
        refreshList()
        val numOfAllFeeds = dbAdapter.numOfFeeds
        if (numOfAllFeeds == 0) {
            view.hideAllUnreadView()
        } else {
            updateAllUnreadArticlesCount()
        }
        view.init(feeds)
    }

    fun isAllRssShowView(position: Int): Boolean {
        return isHided && position == feeds.size
    }

    fun isHideReadRssView(position: Int): Boolean {
        return !isHided && position == allFeeds.size
    }
}
