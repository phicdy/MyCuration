package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.ReadArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReadArticleActionCreator(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val rssRepository: RssRepository,
        private val position: Int,
        private val items: List<ArticleItem>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            when (val item = items[position]) {
                is ArticleItem.Advertisement -> return@withContext
                is ArticleItem.Content -> {
                    val oldStatus = item.value.status
                    if (oldStatus == Article.TOREAD || oldStatus == Article.READ) {
                        return@withContext
                    }
                    val article = item.value
                    articleRepository.saveStatus(article.id, Article.TOREAD)
                    val rss = rssRepository.getFeedById(article.feedId)
                    rss?.let {
                        rssRepository.updateUnreadArticleCount(article.feedId, rss.unreadAriticlesCount - 1)
                        dispatcher.dispatch(ReadArticleAction(ReadArticle(rss.id, 1)))
                    }
                    article.status = Article.TOREAD
                    dispatcher.dispatch(ReadArticlePositionAction(position))
                }
            }
        }
    }
}