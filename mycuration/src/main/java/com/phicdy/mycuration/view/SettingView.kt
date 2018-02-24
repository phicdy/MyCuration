package com.phicdy.mycuration.view

interface SettingView {
    fun setUpdateInterval(index: Int, summary: String)
    fun setAutoUpdateInMainUi(isAutoUpdateInMainUi: Boolean)
    fun setArticleSort(isNewArticleTop: Boolean)
    fun setInternalBrowser(isEnabled: Boolean)
    fun setAllReadBehavior(index: Int, summary: String)
    fun setSwipeDirection(index: Int, summary: String)
    fun startLicenseActivity()
}
