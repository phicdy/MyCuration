package com.phicdy.mycuration.presentation.view

import com.phicdy.mycuration.data.rss.Feed

import java.util.ArrayList

interface RssListView {
    fun showDeleteFeedAlertDialog(position: Int)
    fun showEditTitleDialog(position: Int, feedTitle: String)
    fun setRefreshing(doScroll: Boolean)
    fun init(feeds: ArrayList<Feed>)
    fun setTotalUnreadCount(count: Int)
    fun onRefreshCompleted()
    fun showEditFeedTitleEmptyErrorToast()
    fun showEditFeedFailToast()
    fun showEditFeedSuccessToast()
    fun showDeleteSuccessToast()
    fun showDeleteFailToast()
    fun showAddFeedSuccessToast()
    fun showGenericAddFeedErrorToast()
    fun showInvalidUrlAddFeedErrorToast()
    fun notifyDataSetChanged()
    fun showAllUnreadView()
    fun hideAllUnreadView()
}
