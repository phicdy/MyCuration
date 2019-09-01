package com.phicdy.mycuration.rss

interface RssListView {
    fun showDeleteFeedAlertDialog(rssId: Int, position: Int)
    fun setRefreshing(doScroll: Boolean)
    fun init(items: List<RssListItem>)
    fun setTotalUnreadCount(count: Int)
    fun onRefreshCompleted()
    fun showAddFeedSuccessToast()
    fun showGenericAddFeedErrorToast()
    fun showInvalidUrlAddFeedErrorToast()
    fun notifyDataSetChanged(items: List<RssListItem>)
    fun showAllUnreadView()
    fun hideAllUnreadView()
    fun showRecyclerView()
    fun hideRecyclerView()
    fun showEmptyView()
    fun hideEmptyView()
}
