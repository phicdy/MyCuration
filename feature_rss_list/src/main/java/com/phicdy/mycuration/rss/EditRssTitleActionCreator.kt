package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.ActionCreator2
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.RssRepository
import javax.inject.Inject

class EditRssTitleActionCreator @Inject constructor(
    private val dispatcher: Dispatcher,
    private val rssRepository: RssRepository
): ActionCreator2<String, Int> {
    override suspend fun run(newTitle: String, rssId: Int) {
        if (newTitle.isBlank()) {
            dispatcher.dispatch(EditRssTitleErrorAction(RssListMessage.Type.ERROR_EMPTY_RSS_TITLE_EDIT))
        } else {
            val numOfUpdate = rssRepository.saveNewTitle(rssId, newTitle)
            if (numOfUpdate == 1) {
                dispatcher.dispatch(EditRssTitleSuccessAction(EditRssTitleValue(newTitle, rssId)))
            } else {
                dispatcher.dispatch(EditRssTitleErrorAction(RssListMessage.Type.ERROR_SAVE_RSS_TITLE))
            }
        }
    }
}