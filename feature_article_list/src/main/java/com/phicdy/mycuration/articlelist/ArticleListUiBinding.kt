package com.phicdy.mycuration.articlelist

sealed class ArticleListUiBinding {
    object Init : ArticleListUiBinding()
    data class Loaded(val list: List<ArticleItem>) : ArticleListUiBinding()
    data class Searched(val list: List<ArticleItem>) : ArticleListUiBinding()
}

/** Raw list of the binding, which position based action creators use. */
fun ArticleListUiBinding.articles(): List<ArticleItem> = when (this) {
    ArticleListUiBinding.Init -> emptyList()
    is ArticleListUiBinding.Loaded -> list
    is ArticleListUiBinding.Searched -> list
}
