package com.phicdy.mycuration.curatedarticlelist.action

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.curatedarticlelist.CuratedArticleItem
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReadAllCuratedArticlesActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val articleRepository: ArticleRepository,
        private val rssRepository: RssRepository
) : ActionCreator1<List<CuratedArticleItem>> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(items: List<CuratedArticleItem>) {
        withContext(Dispatchers.IO) {
            val rssCache = hashMapOf<Int, Feed>()
            for (item in items) {
                if (item is CuratedArticleItem.Content) {
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
                    }
                }
            }
        }
        dispatcher.dispatch(ReadALlArticlesAction(Unit))
    }
}
