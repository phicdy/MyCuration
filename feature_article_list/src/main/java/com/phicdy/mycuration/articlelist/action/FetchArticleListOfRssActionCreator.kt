package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchArticleListOfRssActionCreator(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val preferenceHelper: PreferenceHelper,
        private val rssId: Int
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            val allArticles = articleRepository.getUnreadArticlesOfRss(rssId, preferenceHelper.sortNewArticleTop)
            if (allArticles.isEmpty() && articleRepository.isExistArticleOf(rssId)) {
                dispatcher.dispatch(FetchArticleAction(articleRepository.getAllArticlesOfRss(rssId, preferenceHelper.sortNewArticleTop)))
            } else {
                dispatcher.dispatch(FetchArticleAction(allArticles))
            }
        }
    }

}