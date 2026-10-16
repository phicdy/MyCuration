package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.articlelist.toSelectedArticle
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
    override suspend fun run(item: ArticleItem): Unit = run(item, isTwoPane = false)

    /**
     * Opens the article in the detail pane when two panes are shown and the "open internal"
     * setting is on. Otherwise it opens in Custom Tabs or the external browser as before.
     */
    suspend fun run(item: ArticleItem, isTwoPane: Boolean) {
        if (item !is ArticleItem.Content) return
        withContext(Dispatchers.IO) {
            val content = item.value
            when {
                preferenceHelper.isOpenInternal && isTwoPane -> dispatcher.dispatch(
                        OpenDetailPaneAction(content.toSelectedArticle())
                )
                preferenceHelper.isOpenInternal -> dispatcher.dispatch(OpenInternalBrowserAction(content.url))
                else -> dispatcher.dispatch(OpenExternalBrowserAction(content.url))
            }
        }
    }
}