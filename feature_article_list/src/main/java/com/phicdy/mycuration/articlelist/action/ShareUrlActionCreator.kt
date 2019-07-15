package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShareUrlActionCreator(
        private val dispatcher: Dispatcher,
        private val position: Int,
        private val articles: List<Article>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            if (position < 0 || position >= articles.size) return@withContext
            dispatcher.dispatch(ShareUrlAction(articles[position].url))
        }
    }
}