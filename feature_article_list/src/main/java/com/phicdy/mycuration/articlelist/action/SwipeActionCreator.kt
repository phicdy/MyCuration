package com.phicdy.mycuration.articlelist.action

import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SwipeActionCreator(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val unreadCountRepository: UnreadCountRepository,
        private val preferenceHelper: PreferenceHelper,
        private val position: Int,
        private val direction: Int,
        private val articles: List<Article>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            suspend fun update(newStatus: String) {
                if (articles[position].status == newStatus) return
                articles[position].status = newStatus
                dispatcher.dispatch(SwipeAction(position))
                articleRepository.saveStatus(articles[position].id, newStatus)
                if (newStatus == Article.TOREAD) {
                    unreadCountRepository.countDownUnreadCount(articles[position].feedId)
                } else {
                    unreadCountRepository.conutUpUnreadCount(articles[position].feedId)
                }
            }

            when (direction) {
                LEFT -> {
                    when (preferenceHelper.swipeDirection) {
                        PreferenceHelper.SWIPE_RIGHT_TO_LEFT -> update(Article.TOREAD)
                        PreferenceHelper.SWIPE_LEFT_TO_RIGHT -> update(Article.UNREAD)
                        else -> {
                        }
                    }
                }
                RIGHT -> {
                    when (preferenceHelper.swipeDirection) {
                        PreferenceHelper.SWIPE_RIGHT_TO_LEFT -> update(Article.UNREAD)
                        PreferenceHelper.SWIPE_LEFT_TO_RIGHT -> update(Article.TOREAD)
                        else -> {
                        }
                    }
                }
            }
        }
    }
}