package com.phicdy.mycuration.presentation.view


interface TopActivityView {
    fun initViewPager()
    fun initFab()
    fun initToolbar()
    fun setAlarmManager()
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
    fun showEditFeedTitleEmptyErrorToast()
    fun showEditFeedFailToast()
    fun showEditFeedSuccessToast()
    fun updateFeedTitle(rssId: Int, newTitle: String)
    suspend fun removeRss(rssId: Int)
    fun showDeleteSuccessToast()
    fun showDeleteFailToast()
}
