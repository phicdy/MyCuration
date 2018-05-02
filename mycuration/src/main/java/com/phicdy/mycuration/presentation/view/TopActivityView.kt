package com.phicdy.mycuration.presentation.view


interface TopActivityView {
    fun initViewPager()
    fun initFab()
    fun setAlarmManager()
    fun startFabAnimation()
    fun closeAddFab()
    fun closeSearchView()
    fun goToFeedSearch()
    fun goToAddCuration()
    fun goToAddFilter()
    fun goToSetting()
    fun goToArticleSearchResult(query: String)
    fun changeTab(position: Int)
    fun currentTabPosition(): Int
}
