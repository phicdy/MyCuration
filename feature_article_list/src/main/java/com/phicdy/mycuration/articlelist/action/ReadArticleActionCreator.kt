package com.phicdy.mycuration.articlelist.action

import com.phicdy.action.articlelist.ReadArticleAction
import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator2
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.ReadArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReadArticleActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val rssRepository: RssRepository
) : ActionCreator2<Int, List<ArticleItem>> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(position: Int, items: List<ArticleItem>) {
        withContext(Dispatchers.IO) {
            when (val item = items[position]) {
                is ArticleItem.Advertisement -> return@withContext
                is ArticleItem.Content -> {
                    val oldStatus = item.value.status
                    if (oldStatus == Article.READ) {
                        return@withContext
                    }
                    val article = item.value
                    articleRepository.saveStatus(article.id, Article.READ)
                    val rss = rssRepository.getFeedById(article.feedId)
                    rss?.let {
                        rssRepository.updateUnreadArticleCount(article.feedId, rss.unreadAriticlesCount - 1)
                        dispatcher.dispatch(ReadArticleAction(ReadArticle(rss.id, 1)))
                    }
                    article.status = Article.READ
                    dispatcher.dispatch(ReadArticlePositionAction(position))
                }
            }
        }
    }
}