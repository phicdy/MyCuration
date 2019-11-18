package com.phicdy.mycuration.rss

interface RssListView {
    fun init(items: List<RssListItem>)
    fun notifyDataSetChanged(items: List<RssListItem>)
    fun hideRecyclerView()
    fun showEmptyView()
}
