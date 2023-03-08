package com.phicdy.mycuration.feature.addcuration

import com.phicdy.mycuration.core.ActionCreator2
import com.phicdy.mycuration.core.Dispatcher
import javax.inject.Inject

class UpdateTextFieldActionCreator @Inject constructor(
    private val dispatcher: Dispatcher,
): ActionCreator2<AddCurationTextFieldType, String> {

    override suspend fun run(arg1: AddCurationTextFieldType, arg2: String) {
        dispatcher.dispatch(UpdateTextFieldAction(UpdateTextFieldEvent(arg1, arg2)))
    }
}

