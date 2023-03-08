package com.phicdy.mycuration.feature.addcuration

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import javax.inject.Inject

class DeleteCurationWordActionCreator @Inject constructor(
        private val dispatcher: Dispatcher
): ActionCreator1<Int> {
    override suspend fun run(arg: Int) {
        dispatcher.dispatch(DeleteCurationWordAction(arg))
    }
}