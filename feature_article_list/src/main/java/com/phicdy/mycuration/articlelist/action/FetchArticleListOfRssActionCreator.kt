package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FetchArticleListOfRssActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val preferenceHelper: PreferenceHelper
) : ActionCreator1<Int> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(rssId: Int) {
        withContext(Dispatchers.IO) {
            val allArticles = articleRepository.getUnreadArticlesOfRss(rssId, preferenceHelper.sortNewArticleTop)
            if (allArticles.isEmpty() && articleRepository.isExistArticleOf(rssId)) {
                mutableListOf<ArticleItem>().apply {
                    add(ArticleItem.Advertisement)
                    articleRepository.getAllArticlesOfRss(rssId, preferenceHelper.sortNewArticleTop)
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