package com.phicdy.mycuration.articlelist

import androidx.lifecycle.MutableLiveData
import com.phicdy.action.articlelist.ReadAllArticlesAction
import com.phicdy.mycuration.articlelist.action.FinishAction
import com.phicdy.mycuration.articlelist.action.OpenExternalBrowserAction
import com.phicdy.mycuration.articlelist.action.OpenInternalBrowserAction
import com.phicdy.mycuration.articlelist.action.ReadArticlePositionAction
import com.phicdy.mycuration.articlelist.action.ScrollAction
import com.phicdy.mycuration.articlelist.action.ShareUrlAction
import com.phicdy.mycuration.articlelist.action.SwipeAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Reducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class ArticleListReducer(
        private val coroutineScope: CoroutineScope,
        private val channel: Channel<Interation>,
        private val binding: MutableLiveData<ArticleListUiBinding>
): Reducer {
    override fun reduce(action: Action<*>) {
        fun send(interation: Interation) {
            coroutineScope.launch { channel.send(interation) }
        }
        when (action) {
            is ScrollAction -> send(Interation.Scroll(action.value))
            is OpenInternalBrowserAction -> send(Interation.OpenInternalWebBrowser(action.value))
            is OpenExternalBrowserAction -> send(Interation.OpenExternalWebBrowser(action.value))
            is ShareUrlAction -> send(Interation.Share(action.value))
            is ReadArticlePositionAction -> send(Interation.ReadArticle(action.value))
            is ReadAllArticlesAction -> send(Interation.ReadAllOfArticles)
            is SwipeAction -> send(Interation.SwipeArtilce(action.value))
            is FinishAction -> send(Interation.Finish)
        }
    }
}
