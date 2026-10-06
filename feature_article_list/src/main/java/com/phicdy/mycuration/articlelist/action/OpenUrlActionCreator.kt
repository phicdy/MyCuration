package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.articlelist.SelectedArticle
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
        run(item, isTwoPane = false)
    }

    /**
     * @param isTwoPane true when the screen can show the article in the detail pane.
     * The detail pane is used only when the "open internal" setting is enabled.
     */
    suspend fun run(item: ArticleItem, isTwoPane: Boolean) {
        if (item !is ArticleItem.Content) return
        withContext(Dispatchers.IO) {
            val content = item.value
            when {
                preferenceHelper.isOpenInternal && isTwoPane -> dispatcher.dispatch(
                        OpenDetailPaneAction(SelectedArticle(content.id, content.title, content.url))
                )
                preferenceHelper.isOpenInternal -> dispatcher.dispatch(OpenInternalBrowserAction(content.url))
                else -> dispatcher.dispatch(OpenExternalBrowserAction(content.url))
            }
        }
    }
}
