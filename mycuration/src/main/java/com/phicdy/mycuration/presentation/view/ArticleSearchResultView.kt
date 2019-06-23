package com.phicdy.mycuration.presentation.view

import com.phicdy.mycuration.domain.entity.Article

import java.util.ArrayList

interface ArticleSearchResultView {
    fun refreshList(articles: ArrayList<Article>)
    fun startInternalWebView(url: String)
    fun startExternalWebView(url: String)
    fun startShareUrl(url: String)
}
