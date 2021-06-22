package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Curation
import com.phicdy.mycuration.presentation.view.CurationItem
import com.phicdy.mycuration.presentation.view.CurationListView
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class CurationListPresenter @Inject constructor(
        private val view: CurationListView,
        private val rssRepository: RssRepository,
        private val curationRepository: CurationRepository
) {
    private var allCurations: List<Curation> = arrayListOf()

    suspend fun resume() = coroutineScope {
        view.registerContextMenu()
        allCurations = curationRepository.getAllCurations()
        if (allCurations.isEmpty()) {
            view.hideRecyclerView()
            view.showEmptyView()
        } else {
            view.hideEmptyView()
            view.showRecyclerView()
            view.initListBy(allCurations)
        }
    }

    fun onCurationEditClicked(curationId: Int) {
        if (curationId < 0) return
        view.startEditCurationActivity(curationId)
    }

    suspend fun onCurationDeleteClicked(curation: Curation, size: Int) = coroutineScope {
        curationRepository.delete(curation.id)
        if (size == 1) {
            view.hideRecyclerView()
            view.showEmptyView()
        }
    }

    fun activityCreated() {
        val num = rssRepository.getNumOfRss()
        if (num == 0L) {
            view.setNoRssTextToEmptyView()
        }
    }

    suspend fun getView(curation: Curation?, item: CurationItem) {
        curation?.let {
            item.setName(it.name)
            val count = curationRepository.calcNumOfAllUnreadArticlesOfCuration(curation.id)
            item.setCount(count.toString())
        }
    }
}
