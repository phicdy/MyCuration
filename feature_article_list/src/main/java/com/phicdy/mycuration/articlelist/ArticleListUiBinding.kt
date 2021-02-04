package com.phicdy.mycuration.articlelist

sealed class ArticleListUiBinding {
    object Init : ArticleListUiBinding()
    data class Loaded(val list: List<ArticleItem>) : ArticleListUiBinding()
    data class Searched(val list: List<ArticleItem>) : ArticleListUiBinding()
}
