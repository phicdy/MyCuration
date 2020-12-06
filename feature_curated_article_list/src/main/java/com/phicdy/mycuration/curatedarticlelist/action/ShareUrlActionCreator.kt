package com.phicdy.mycuration.curatedarticlelist.action

import com.phicdy.mycuration.core.ActionCreator2
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.curatedarticlelist.CuratedArticleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ShareUrlActionCreator @Inject constructor(
        private val dispatcher: Dispatcher
) : ActionCreator2<Int, List<CuratedArticleItem>> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(position: Int, items: List<CuratedArticleItem>) {
        withContext(Dispatchers.IO) {
            if (position < 0 || position >= items.size) return@withContext
            when (val item = items[position]) {
                is CuratedArticleItem.Content -> dispatcher.dispatch(ShareUrlAction(item.value.url))
            }
        }
    }
}