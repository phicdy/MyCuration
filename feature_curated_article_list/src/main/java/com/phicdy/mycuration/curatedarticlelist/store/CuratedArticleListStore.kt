package com.phicdy.mycuration.curatedarticlelist.store

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import com.phicdy.mycuration.curatedarticlelist.CuratedArticleItem
import com.phicdy.mycuration.curatedarticlelist.action.FetchArticleAction
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CuratedArticleListStore @Inject constructor(
        dispatcher: Dispatcher
) : Store<List<CuratedArticleItem>>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is FetchArticleAction -> _state.value = action.value
        }
    }
}