package com.phicdy.mycuration.curatedarticlelist.action

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.curatedarticlelist.CuratedArticleItem
import com.phicdy.mycuration.data.preference.PreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OpenUrlActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val preferenceHelper: PreferenceHelper
) : ActionCreator1<CuratedArticleItem> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(item: CuratedArticleItem) {
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