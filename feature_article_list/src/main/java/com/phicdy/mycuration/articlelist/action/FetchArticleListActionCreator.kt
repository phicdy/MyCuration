package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchAllArticleListActionCreator(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val preferenceHelper: PreferenceHelper
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            val allArticles = articleRepository.getAllUnreadArticles(preferenceHelper.sortNewArticleTop)
            if (allArticles.isEmpty() && articleRepository.isExistArticle()) {
                dispatcher.dispatch(FetchArticleAction(articleRepository.getTop300Articles(preferenceHelper.sortNewArticleTop)))
            } else {
                dispatcher.dispatch(FetchArticleAction(allArticles))
            }
        }
    }
}