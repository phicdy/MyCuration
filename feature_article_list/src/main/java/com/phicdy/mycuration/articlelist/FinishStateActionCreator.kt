package com.phicdy.mycuration.articlelist

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FinishStateActionCreator(
        private val dispatcher: Dispatcher,
        private val preferenceHelper: PreferenceHelper,
        private val articles: List<Article>
) : ActionCreator {

    override suspend fun run() {
        withContext(Dispatchers.IO) {
            if (!preferenceHelper.allReadBack) return@withContext
            for (article in articles) {
                if (article.status == Article.UNREAD) {
                    return@withContext
                }
            }
            dispatcher.dispatch(FinishAction(Unit))
        }
    }
}