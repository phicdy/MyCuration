package com.phicdy.mycuration.curatedarticlelist.action

import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.curatedarticlelist.CuratedArticleItem
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SwipeActionCreator(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val rssRepository: RssRepository,
        private val preferenceHelper: PreferenceHelper,
        private val position: Int,
        private val direction: Int,
        private val items: List<CuratedArticleItem>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            suspend fun update(newStatus: String) {
                when (val item = items[position]) {
                    is CuratedArticleItem.Content -> {
                        val article = item.value
                        if (article.status == newStatus) {
                            dispatcher.dispatch(SwipeAction(position))
                            return
                        }
                        article.status = newStatus
                        dispatcher.dispatch(SwipeAction(position))
                        articleRepository.saveStatus(article.id, newStatus)
                        val rss = rssRepository.getFeedById(article.feedId)
                        rss?.let {
                            if (newStatus == Article.TOREAD) {
                                rssRepository.updateUnreadArticleCount(rss.id, rss.unreadAriticlesCount - 1)
                            } else {
                                rssRepository.updateUnreadArticleCount(rss.id, rss.unreadAriticlesCount + 1)
                            }
                        }
                    }
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