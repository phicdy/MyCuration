package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchArticleListActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val preferenceHelper: PreferenceHelper
) : ActionCreator1<String> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(query: String) {
        withContext(Dispatchers.IO) {
            articleRepository.searchArticles(query, preferenceHelper.sortNewArticleTop)
                    .map { ArticleItem.Content(it) }
                    .let { dispatcher.dispatch(SearchArticleAction(it)) }
        }
    }

}