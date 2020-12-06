package com.phicdy.mycuration.curatedarticlelist.action

import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import com.phicdy.mycuration.core.ActionCreator3
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.curatedarticlelist.CuratedArticleItem
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SwipeActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val rssRepository: RssRepository,
        private val preferenceHelper: PreferenceHelper
) : ActionCreator3<Int, Int, List<CuratedArticleItem>> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(position: Int, direction: Int, items: List<CuratedArticleItem>) {
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
                            if (newStatus == Article.READ) {
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
                        PreferenceHelper.SWIPE_RIGHT_TO_LEFT -> update(Article.READ)
                        PreferenceHelper.SWIPE_LEFT_TO_RIGHT -> update(Article.UNREAD)
                        else -> {
                        }
                    }
                }
                RIGHT -> {
                    when (preferenceHelper.swipeDirection) {
                        PreferenceHelper.SWIPE_RIGHT_TO_LEFT -> update(Article.UNREAD)
                        PreferenceHelper.SWIPE_LEFT_TO_RIGHT -> update(Article.READ)
                        else -> {
                        }
                    }
                }
            }
        }
    }
}