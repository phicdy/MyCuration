package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenUrlActionCreator(
        private val dispatcher: Dispatcher,
        private val preferenceHelper: PreferenceHelper,
        private val feedId: Int,
        private val item: ArticleItem,
        private val rssRepository: RssRepository
) : ActionCreator {

    override suspend fun run() {
        if (item is ArticleItem.Advertisement) return
        withContext(Dispatchers.IO) {
            when (item) {
                is ArticleItem.Advertisement -> return@withContext
                is ArticleItem.Content -> {
                    val content = item.value
                    if (preferenceHelper.isOpenInternal) {
                        if (feedId == Feed.ALL_FEED_ID) {
                            dispatcher.dispatch(OpenInternalBrowserAction(content))
                        } else {
                            val feed = rssRepository.getFeedById(feedId)
                            val article = content.copy(feedTitle = feed?.title ?: "")
                            dispatcher.dispatch(OpenInternalBrowserAction(article))
                        }
                    } else {
                        dispatcher.dispatch(OpenExternalBrowserAction(content.url))
                    }
                }
            }
        }
    }
}