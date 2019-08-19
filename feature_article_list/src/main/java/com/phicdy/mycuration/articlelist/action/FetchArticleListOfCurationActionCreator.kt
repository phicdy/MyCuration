package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchArticleListOfCurationActionCreator(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val preferenceHelper: PreferenceHelper,
        private val curationId: Int
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            val allArticles = articleRepository.getAllUnreadArticlesOfCuration(curationId, preferenceHelper.sortNewArticleTop)
            if (allArticles.isEmpty()) {
                mutableListOf<ArticleItem>().apply {
                    add(ArticleItem.Advertisement)
                    articleRepository.getAllArticlesOfCuration(curationId, preferenceHelper.sortNewArticleTop)
                            .map { ArticleItem.Content(it) }
                            .let(::addAll)
                }.let { dispatcher.dispatch(FetchArticleAction(it)) }
            } else {
                mutableListOf<ArticleItem>().apply {
                    add(ArticleItem.Advertisement)
                    allArticles.map { ArticleItem.Content(it) }
                            .let(::addAll)
                }.let { dispatcher.dispatch(FetchArticleAction(it)) }
            }
        }
    }

}