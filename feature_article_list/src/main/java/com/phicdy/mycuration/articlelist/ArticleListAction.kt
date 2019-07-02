package com.phicdy.mycuration.articlelist

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.entity.Article

sealed class ArticleListAction<out T>(override val type: String) : Action<T>

data class FetchArticleAction(
        override val value: List<Article>
) : ArticleListAction<List<Article>>(TYPE) {
    companion object {
        const val TYPE = "FetchArticleActionType"
    }
}
