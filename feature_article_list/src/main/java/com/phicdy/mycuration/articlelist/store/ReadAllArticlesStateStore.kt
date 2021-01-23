package com.phicdy.mycuration.articlelist.store

import com.phicdy.action.articlelist.ReadAllArticlesAction
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.core.Store
import com.phicdy.mycuration.entity.ReadAllArticles
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReadAllArticlesStateStore @Inject constructor(
        dispatcher: Dispatcher
) : Store<ReadAllArticles>(dispatcher) {

    init {
        dispatcher.register(this)
    }

    override suspend fun notify(action: Action<*>) {
        when (action) {
            is ReadAllArticlesAction -> _state.value = action.value
        }
    }
}