package com.phicdy.mycuration.presentation.view

interface ArticleListView {
    val firstVisiblePosition: Int
    val lastVisiblePosition: Int
    val isBottomVisible: Boolean
    fun openInternalWebView(url: String, rssTitle: String)
    fun openExternalWebView(url: String)
    fun notifyListView()
    fun finish()
    fun showShareUi(url: String)
    fun scrollTo(position: Int)
    fun showEmptyView()
    fun showNoSearchResult()
}
