package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenUrlActionCreator(
        private val dispatcher: Dispatcher,
        private val preferenceHelper: PreferenceHelper,
        private val feedId: Int,
        private val article: Article,
        private val rssRepository: RssRepository
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            if (preferenceHelper.isOpenInternal) {
                if (feedId == Feed.ALL_FEED_ID) {
                    dispatcher.dispatch(OpenInternalBrowserAction(article))
                } else {
                    val feed = rssRepository.getFeedById(feedId)
                    val article = article.copy(feedTitle = feed?.title ?: "")
                    dispatcher.dispatch(OpenInternalBrowserAction(article))
                }
            } else {
                dispatcher.dispatch(OpenExternalBrowserAction(article.url))
            }
        }
    }
}