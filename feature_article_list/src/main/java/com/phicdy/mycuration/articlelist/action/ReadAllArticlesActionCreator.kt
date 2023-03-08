package com.phicdy.mycuration.articlelist.action

import com.phicdy.action.articlelist.ReadAllArticlesAction
import com.phicdy.action.articlelist.ReadArticleAction
import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.entity.ReadAllArticles
import com.phicdy.mycuration.entity.ReadArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReadAllArticlesActionCreator(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val rssRepository: RssRepository,
        private val feedId: Int,
        private val items: List<ArticleItem>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            val unread = items.filterIsInstance<ArticleItem.Content>()
                    .filter { it.value.status == Article.UNREAD }
            if (feedId == Feed.ALL_FEED_ID) {
                articleRepository.saveAllStatusToRead()

                val allRss = rssRepository.getAllFeedsWithNumOfUnreadArticles()
                allRss.forEach { rss ->
                    val readCount = unread.filter { rss.id == it.value.feedId }.size
                    if (readCount == 0) return@forEach
                    rssRepository.updateUnreadArticleCount(rss.id, readCount)
                    dispatcher.dispatch(ReadArticleAction(ReadArticle(rss.id, readCount)))
                }
            } else {
                rssRepository.getFeedById(feedId)?.let { rss ->
                    val readCount = unread.filter { rss.id == it.value.feedId }.size
                    if (readCount == 0) return@let
                    articleRepository.saveStatusToRead(feedId)
                    rssRepository.updateUnreadArticleCount(feedId, 0)
                    dispatcher.dispatch(ReadArticleAction(ReadArticle(rss.id, readCount)))
                }
            }

            for (item in unread) {
                item.value.status = Article.READ
            }
            dispatcher.dispatch(ReadAllArticlesAction(ReadAllArticles(feedId)))
        }
    }
}