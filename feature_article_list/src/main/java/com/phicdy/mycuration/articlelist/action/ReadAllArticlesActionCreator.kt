package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
            val changeStatus = async {
                for (item in items) {
                    when(item) {
                        is ArticleItem.Content -> item.value.status = Article.READ
                    }
                }
            }
            val updateRepository = async {
                if (feedId == Feed.ALL_FEED_ID) {
                    articleRepository.saveAllStatusToRead()
                    val allFeeds = rssRepository.getAllFeedsWithNumOfUnreadArticles()
                    allFeeds.forEach {
                        rssRepository.updateUnreadArticleCount(it.id, 0)
                    }
                } else {
                    articleRepository.saveStatusToRead(feedId)
                    rssRepository.updateUnreadArticleCount(feedId, 0)
                }
            }
            changeStatus.await()
            updateRepository.await()
            dispatcher.dispatch(ReadALlArticlesAction(Unit))
        }
    }
}