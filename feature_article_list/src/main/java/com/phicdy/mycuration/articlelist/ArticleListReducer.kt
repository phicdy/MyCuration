package com.phicdy.mycuration.articlelist

import androidx.lifecycle.MutableLiveData
import com.phicdy.mycuration.articlelist.action.ScrollAction
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
        when (action) {
            is ScrollAction -> {
                coroutineScope.launch {
                    channel.send(Interation.Scroll(action.value))
                }
            }
        }
    }
}
