package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FetchAllArticleListActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val preferenceHelper: PreferenceHelper
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            val allUnreadArticles = articleRepository.getAllUnreadArticles(preferenceHelper.sortNewArticleTop)
            if (allUnreadArticles.isEmpty()) {
                mutableListOf<ArticleItem>().apply {
                    add(ArticleItem.Advertisement)
                    articleRepository.getTop300Articles(preferenceHelper.sortNewArticleTop)
                            .map { ArticleItem.Content(it) }
                            .let(::addAll)
                }.let { dispatcher.dispatch(FetchArticleAction(it)) }
            } else {
                mutableListOf<ArticleItem>().apply {
                    add(ArticleItem.Advertisement)
                    allUnreadArticles.map { ArticleItem.Content(it) }
                            .let(::addAll)
                }.let { dispatcher.dispatch(FetchArticleAction(it)) }
            }
        }
    }
}