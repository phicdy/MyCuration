package com.phicdy.mycuration.rss

import com.phicdy.mycuration.data.repository.RssRepository
import javax.inject.Inject

class RssListFragmentPresenter @Inject constructor(
    private val view: RssListFragment,
    private val rssRepository: RssRepository
) {

    suspend fun onDeleteOkButtonClicked(rssId: Int) {
        if (rssRepository.deleteRss(rssId)) {
            view.removeRss(rssId)
            view.showDeleteSuccessToast()
        } else {
            view.showDeleteFailToast()
        }
    }
}
