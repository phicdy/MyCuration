package com.phicdy.mycuration.articlelist

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReadArticleActionCreator(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val unreadCountRepository: UnreadCountRepository,
        private val position: Int,
        private val articles: List<Article>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            val oldStatus = articles[position].status
            if (oldStatus == Article.TOREAD || oldStatus == Article.READ) {
                return@withContext
            }
            articleRepository.saveStatus(articles[position].id, Article.TOREAD)
            unreadCountRepository.countDownUnreadCount(articles[position].feedId)
            articles[position].status = Article.TOREAD
            dispatcher.dispatch(ReadArticleAction(position))
        }
    }
}