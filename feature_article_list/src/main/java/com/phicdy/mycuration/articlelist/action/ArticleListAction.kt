package com.phicdy.mycuration.articlelist.action

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

data class ReadArticleAction(
        override val value: Int
) : ArticleListAction<Int>(TYPE) {
    companion object {
        const val TYPE = "ReadArticleActionType"
    }
}

data class FinishAction(
        override val value: Unit
) : ArticleListAction<Unit>(TYPE) {
    companion object {
        const val TYPE = "FinishActionType"
    }
}
