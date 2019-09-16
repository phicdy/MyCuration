package com.phicdy.mycuration.rss

interface RssListView {
    fun showDeleteFeedAlertDialog(rssId: Int, position: Int)
    fun setRefreshing(doScroll: Boolean)
    fun init(items: List<RssListItem>)
    fun onRefreshCompleted()
    fun showAddFeedSuccessToast()
    fun showGenericAddFeedErrorToast()
    fun showInvalidUrlAddFeedErrorToast()
    fun notifyDataSetChanged(items: List<RssListItem>)
    fun showRecyclerView()
    fun hideRecyclerView()
    fun showEmptyView()
    fun hideEmptyView()
}
