package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchArticleListActionCreator(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val preferenceHelper: PreferenceHelper,
        private val query: String
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            dispatcher.dispatch(SearchArticleAction(articleRepository.searchArticles(query, preferenceHelper.sortNewArticleTop)))
        }
    }

}