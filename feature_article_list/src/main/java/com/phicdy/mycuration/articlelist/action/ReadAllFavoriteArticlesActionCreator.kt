package com.phicdy.mycuration.articlelist.action

import com.phicdy.action.articlelist.ReadArticleAction
import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.ReadArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReadAllFavoriteArticlesActionCreator(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val rssRepository: RssRepository,
        private val items: List<ArticleItem>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            val rssCache = hashMapOf<Int, Feed>()
            for (item in items) {
                when (item) {
                    is ArticleItem.Content -> {
                        val content = item.value
                        content.status = Article.READ
                        articleRepository.saveStatus(content.id, Article.READ)
                        val rss = if (rssCache[content.feedId] == null) {
                            val cache = rssRepository.getFeedById(content.feedId)
                            cache?.let { rssCache[content.feedId] = cache }
                            cache
                        } else {
                            rssCache[content.feedId]
                        }
                        rss?.let {
                            rssRepository.updateUnreadArticleCount(content.feedId, it.unreadAriticlesCount - 1)
                            dispatcher.dispatch(ReadArticleAction(ReadArticle(content.feedId, 1)))
                        }
                    }
                }
            }
        }
    }
}