package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScrollActionCreator(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val rssRepository: RssRepository,
        private val firstVisiblePosition: Int,
        private val lastVisiblePosition: Int,
        private val allArticles: List<Article>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            if (allArticles.isEmpty()) return@withContext

            val rssCache = hashMapOf<Int, Feed>()
            for (position in firstVisiblePosition..lastVisiblePosition) {
                if (position > allArticles.size - 1) break
                val targetArticle = allArticles[position]
                if (targetArticle.status == Article.UNREAD) {
                    targetArticle.status = Article.TOREAD

                    val rss = if (rssCache[targetArticle.feedId] == null) {
                        val cache = rssRepository.getFeedById(targetArticle.feedId)
                        cache?.let { rssCache[targetArticle.feedId] = cache }
                        cache
                    } else {
                        rssCache[targetArticle.feedId]
                    }
                    rss?.let {
                        rssRepository.updateUnreadArticleCount(it.id, rss.unreadAriticlesCount - 1)
                        rss.unreadAriticlesCount -= 1
                    }
                    articleRepository.saveStatus(targetArticle.id, Article.TOREAD)
                }
            }

            // Scroll
            val visibleNum = lastVisiblePosition - firstVisiblePosition
            val positionAfterScroll =
                    if (lastVisiblePosition + visibleNum >= allArticles.size - 1) allArticles.size - 1
                    else lastVisiblePosition + visibleNum
            dispatcher.dispatch(ScrollAction(positionAfterScroll))
        }
    }
}