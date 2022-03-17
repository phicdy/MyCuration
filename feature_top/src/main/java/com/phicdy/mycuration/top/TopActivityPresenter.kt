package com.phicdy.mycuration.top

import com.phicdy.mycuration.data.repository.RssRepository
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class TopActivityPresenter @Inject constructor(
    private val view: TopActivityView,
    private val rssRepository: RssRepository
) {

    suspend fun onEditFeedOkButtonClicked(newTitle: String, rssId: Int) = coroutineScope {
        if (newTitle.isBlank()) {
            view.showEditFeedTitleEmptyErrorToast()
        } else {
            val numOfUpdate = rssRepository.saveNewTitle(rssId, newTitle)
            if (numOfUpdate == 1) {
                view.showEditFeedSuccessToast()
                view.updateFeedTitle(rssId, newTitle)
            } else {
                view.showEditFeedFailToast()
            }
        }
    }

    suspend fun onDeleteOkButtonClicked(rssId: Int) {
        if (rssRepository.deleteRss(rssId)) {
            view.removeRss(rssId)
            view.showDeleteSuccessToast()
        } else {
            view.showDeleteFailToast()
        }
    }
}
