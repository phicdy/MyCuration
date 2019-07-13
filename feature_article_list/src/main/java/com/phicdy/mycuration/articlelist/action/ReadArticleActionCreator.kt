package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReadArticleActionCreator(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val rssRepository: RssRepository,
        private val position: Int,
        private val articles: List<Article>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            val oldStatus = articles[position].status
            if (oldStatus == Article.TOREAD || oldStatus == Article.READ) {
                return@withContext
            }
            val article = articles[position]
            articleRepository.saveStatus(article.id, Article.TOREAD)
            val rss = rssRepository.getFeedById(article.feedId)
            rss?.let {
                rssRepository.updateUnreadArticleCount(article.feedId, rss.unreadAriticlesCount - 1)
            }
            articles[position].status = Article.TOREAD
            dispatcher.dispatch(ReadArticleAction(position))
        }
    }
}