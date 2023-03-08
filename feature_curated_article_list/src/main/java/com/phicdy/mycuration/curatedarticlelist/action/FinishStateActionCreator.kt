package com.phicdy.mycuration.curatedarticlelist.action

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.curatedarticlelist.CuratedArticleItem
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FinishStateActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val preferenceHelper: PreferenceHelper
) : ActionCreator1<List<CuratedArticleItem>> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override suspend fun run(items: List<CuratedArticleItem>) {
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