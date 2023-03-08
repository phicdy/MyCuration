package com.phicdy.mycuration.feature.addcuration

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import javax.inject.Inject

class AddCurationWordActionCreator @Inject constructor(
        private val dispatcher: Dispatcher
): ActionCreator1<String> {
    override suspend fun run(arg: String) {
        dispatcher.dispatch(AddCurationWordAction(arg))
    }
}