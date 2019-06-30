package com.phicdy.mycuration.articlelist

interface ArticleItemView {
    fun setArticleTitle(title: String)
    fun setArticleUrl(url: String)
    fun setArticlePostedTime(time: String)
    fun setNotGetPoint()
    fun setArticlePoint(point: String)
    fun hideRssInfo()
    fun setRssTitle(title: String)
    fun setRssIcon(path: String)
    fun setDefaultRssIcon()
    fun changeColorToRead()
    fun changeColorToUnread()
}
