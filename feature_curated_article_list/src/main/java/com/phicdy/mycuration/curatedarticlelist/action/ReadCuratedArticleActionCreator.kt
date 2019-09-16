package com.phicdy.mycuration.curatedarticlelist.action

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.curatedarticlelist.CuratedArticleItem
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReadCuratedArticleActionCreator(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val rssRepository: RssRepository,
        private val position: Int,
        private val items: List<CuratedArticleItem>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            when (val item = items[position]) {
                is CuratedArticleItem.Advertisement -> return@withContext
                is CuratedArticleItem.Content -> {
                    val oldStatus = item.value.status
                    if (oldStatus == Article.TOREAD || oldStatus == Article.READ) {
                        return@withContext
                    }
                    val article = item.value
                    articleRepository.saveStatus(article.id, Article.TOREAD)
                    val rss = rssRepository.getFeedById(article.feedId)
                    rss?.let {
                        rssRepository.updateUnreadArticleCount(article.feedId, rss.unreadAriticlesCount - 1)
                    }
                    article.status = Article.TOREAD
                    dispatcher.dispatch(ReadArticleAction(position))
                }
            }
        }
    }
}