package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OpenUrlActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val preferenceHelper: PreferenceHelper
) : ActionCreator1<ArticleItem> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(item: ArticleItem) {
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