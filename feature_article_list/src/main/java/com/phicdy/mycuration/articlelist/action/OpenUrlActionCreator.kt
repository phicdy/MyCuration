package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenUrlActionCreator(
        private val dispatcher: Dispatcher,
        private val preferenceHelper: PreferenceHelper,
        private val item: ArticleItem
) : ActionCreator {

    override suspend fun run() {
        if (item is ArticleItem.Advertisement) return
        withContext(Dispatchers.IO) {
            when (item) {
                is ArticleItem.Advertisement -> return@withContext
                is ArticleItem.Content -> {
                    val content = item.value
                    if (preferenceHelper.isOpenInternal) {
                        dispatcher.dispatch(OpenInternalBrowserAction(content.url))
                    } else {
                        dispatcher.dispatch(OpenExternalBrowserAction(content.url))
                    }
                }
            }
        }
    }
}