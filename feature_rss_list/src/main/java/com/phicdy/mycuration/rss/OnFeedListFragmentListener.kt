package com.phicdy.mycuration.rss

interface OnFeedListFragmentListener {
    fun onListClicked(feedId: Int)
    fun onEditRssClicked(rssId: Int, feedTitle: String)
    fun onDeleteRssClicked(rssId: Int, position: Int)
    fun onAllUnreadClicked()
    fun onFavoriteClicked()
    fun onFooterClicked()
}
