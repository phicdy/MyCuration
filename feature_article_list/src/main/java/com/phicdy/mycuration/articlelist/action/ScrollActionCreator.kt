package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScrollActionCreator(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val unreadCountRepository: UnreadCountRepository,
        private val firstVisiblePosition: Int,
        private val lastVisiblePosition: Int,
        private val allArticles: List<Article>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            if (allArticles.isEmpty()) return@withContext

            for (position in firstVisiblePosition..lastVisiblePosition) {
                if (position > allArticles.size - 1) break
                val targetArticle = allArticles[position]
                if (targetArticle.status == Article.UNREAD) {
                    targetArticle.status = Article.TOREAD
                    unreadCountRepository.countDownUnreadCount(targetArticle.feedId)
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