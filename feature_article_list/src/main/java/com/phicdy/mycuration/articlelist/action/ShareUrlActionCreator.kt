package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShareUrlActionCreator(
        private val dispatcher: Dispatcher,
        private val position: Int,
        private val items: List<ArticleItem>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            if (position < 0 || position >= items.size) return@withContext
            when(val item = items[position]) {
                is ArticleItem.Content -> dispatcher.dispatch(ShareUrlAction(item.value.url))
            }
        }
    }
}