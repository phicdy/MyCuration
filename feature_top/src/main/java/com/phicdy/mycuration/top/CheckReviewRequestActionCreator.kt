package com.phicdy.mycuration.top

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import javax.inject.Inject

class CheckReviewRequestActionCreator @Inject constructor(
    private val helper: PreferenceHelper,
    private val dispatcher: Dispatcher
) : ActionCreator {
    override suspend fun run() {
        if (!helper.isReviewed() && helper.getReviewCount() - 1 <= 0) {
            dispatcher.dispatch(ShowRateDialogAction(Unit))
            helper.resetReviewCount()
        } else {
            if (helper.getReviewCount() <= 0) {
                helper.resetReviewCount()
            } else {
                helper.decreaseReviewCount()
            }
        }
    }
}