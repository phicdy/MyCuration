package com.phicdy.mycuration.curatedarticlelist.action

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.curatedarticlelist.CuratedArticleItem
import com.phicdy.mycuration.data.preference.PreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenUrlActionCreator(
        private val dispatcher: Dispatcher,
        private val preferenceHelper: PreferenceHelper,
        private val item: CuratedArticleItem
) : ActionCreator {

    override suspend fun run() {
        if (item is CuratedArticleItem.Advertisement) return
        withContext(Dispatchers.IO) {
            when (item) {
                is CuratedArticleItem.Advertisement -> return@withContext
                is CuratedArticleItem.Content -> {
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