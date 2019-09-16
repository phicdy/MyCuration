package com.phicdy.mycuration.curatedarticlelist.action

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.curatedarticlelist.CuratedArticleItem
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FinishStateActionCreator(
        private val dispatcher: Dispatcher,
        private val preferenceHelper: PreferenceHelper,
        private val items: List<CuratedArticleItem>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            if (!preferenceHelper.allReadBack) return@withContext
            loop@ for (item in items) {
                when (item) {
                    is CuratedArticleItem.Advertisement -> continue@loop
                    is CuratedArticleItem.Content -> {
                        if (item.value.status == Article.UNREAD) {
                            return@withContext
                        }
                    }
                }
            }
            dispatcher.dispatch(FinishAction(Unit))
        }
    }
}