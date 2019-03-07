package com.phicdy.mycuration.presentation.view

import com.phicdy.mycuration.data.rss.Feed

import java.util.ArrayList

interface RssListView {
    fun showDeleteFeedAlertDialog(rssId: Int, position: Int)
    fun showEditTitleDialog(rssId: Int, feedTitle: String)
    fun setRefreshing(doScroll: Boolean)
    fun init(feeds: ArrayList<Feed>)
    fun setTotalUnreadCount(count: Int)
    fun onRefreshCompleted()
    fun showAddFeedSuccessToast()
    fun showGenericAddFeedErrorToast()
    fun showInvalidUrlAddFeedErrorToast()
    fun notifyDataSetChanged()
    fun showAllUnreadView()
    fun hideAllUnreadView()
    fun showRecyclerView()
    fun hideRecyclerView()
    fun showEmptyView()
    fun hideEmptyView()
}
