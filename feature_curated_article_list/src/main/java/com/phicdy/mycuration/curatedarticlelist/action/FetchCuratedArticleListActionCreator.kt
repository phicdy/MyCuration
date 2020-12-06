package com.phicdy.mycuration.curatedarticlelist.action

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.curatedarticlelist.CuratedArticleItem
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FetchCuratedArticleListActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val preferenceHelper: PreferenceHelper
) : ActionCreator1<Int> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(curationId: Int) {
        withContext(Dispatchers.IO) {
            val allArticles = articleRepository.getAllUnreadArticlesOfCuration(curationId, preferenceHelper.sortNewArticleTop)
            if (allArticles.isEmpty()) {
                mutableListOf<CuratedArticleItem>().apply {
                    add(CuratedArticleItem.Advertisement)
                    articleRepository.getAllArticlesOfCuration(curationId, preferenceHelper.sortNewArticleTop)
                            .map { CuratedArticleItem.Content(it) }
                            .let(::addAll)
                }.let { dispatcher.dispatch(FetchArticleAction(it)) }
            } else {
                mutableListOf<CuratedArticleItem>().apply {
                    add(CuratedArticleItem.Advertisement)
                    allArticles.map { CuratedArticleItem.Content(it) }
                            .let(::addAll)
                }.let { dispatcher.dispatch(FetchArticleAction(it)) }
            }
        }
    }

}