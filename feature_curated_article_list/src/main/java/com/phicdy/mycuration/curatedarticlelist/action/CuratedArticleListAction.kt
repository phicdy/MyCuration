package com.phicdy.mycuration.curatedarticlelist.action

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.curatedarticlelist.CuratedArticleItem

sealed class ArticleListAction<out T> : Action<T>

data class FetchArticleAction(
        override val value: List<CuratedArticleItem>
) : ArticleListAction<List<CuratedArticleItem>>()

data class ReadArticleAction(
        override val value: Int
) : ArticleListAction<Int>()

data class ReadALlArticlesAction(
        override val value: Unit
) : ArticleListAction<Unit>()

data class FinishAction(
        override val value: Unit
) : ArticleListAction<Unit>()

data class OpenInternalBrowserAction(
        override val value: String
) : ArticleListAction<String>()

data class OpenExternalBrowserAction(
        override val value: String
) : ArticleListAction<String>()

data class ScrollAction(
        override val value: Int
) : ArticleListAction<Int>()

data class SwipeAction(
        override val value: Int
) : ArticleListAction<Int>()

data class ShareUrlAction(
        override val value: String
) : ArticleListAction<String>()

