package com.phicdy.mycuration.feedsearch

interface FeedSearchView {
    fun startFeedUrlHookActivity(url: String)
    fun showProgressBar()
    fun dismissProgressBar()
    fun load(url: String)
    fun showInvalidUrlErrorToast()
    fun showGenericErrorToast()
    fun showAddFeedSuccessToast()
    fun finishView()
    fun setSearchViewTextFrom(url: String)
    fun trackFailedUrl(url: String)
}
