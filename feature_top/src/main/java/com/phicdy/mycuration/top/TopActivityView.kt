package com.phicdy.mycuration.top


interface TopActivityView {
    fun startFabAnimation()
    fun closeAddFab()
    fun closeSearchView()
    fun goToFeedSearch()
    fun goToAddCuration()
    fun goToAddFilter()
    fun goToSetting()
    fun goToArticleSearchResult(query: String)
    fun showRateDialog()
    fun goToGooglePlay()
    fun updateFeedTitle(rssId: Int, newTitle: String)
    suspend fun removeRss(rssId: Int)
}
