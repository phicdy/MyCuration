package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FinishStateActionCreator(
        private val dispatcher: Dispatcher,
        private val preferenceHelper: PreferenceHelper,
        private val items: List<ArticleItem>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            if (!preferenceHelper.allReadBack) return@withContext
            loop@ for (item in items) {
                when (item) {
                    is ArticleItem.Advertisement -> continue@loop
                    is ArticleItem.Content -> {
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