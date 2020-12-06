package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.FavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FetchFavoriteArticleListActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val favoriteRepository: FavoriteRepository,
        private val preferenceHelper: PreferenceHelper
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            val allArticles = favoriteRepository.fetchAll(preferenceHelper.sortNewArticleTop)
            mutableListOf<ArticleItem>().apply {
                add(ArticleItem.Advertisement)
                allArticles.map { ArticleItem.Content(it) }
                        .let(::addAll)
            }.let { dispatcher.dispatch(FetchArticleAction(it)) }
        }
    }
}