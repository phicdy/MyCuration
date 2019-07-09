package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class ReadAllArticlesActionCreator(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val unreadCountRepository: UnreadCountRepository,
        private val feedId: Int,
        private val allArticles: List<Article>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            val changeStatus = async {
                for (article in allArticles) {
                    article.status = Article.READ
                }
            }
            val updateRepository = async {
                if (feedId == Feed.ALL_FEED_ID) {
                    articleRepository.saveAllStatusToRead()
                    unreadCountRepository.readAll()
                } else {
                    articleRepository.saveStatusToRead(feedId)
                    unreadCountRepository.readAll(feedId)
                }
            }
            changeStatus.await()
            updateRepository.await()
            dispatcher.dispatch(ReadALlArticlesAction(Unit))
        }
    }
}