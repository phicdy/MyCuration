package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.domain.entity.Curation
import com.phicdy.mycuration.presentation.view.CurationItem
import com.phicdy.mycuration.presentation.view.CurationListView
import kotlinx.coroutines.coroutineScope
import java.util.ArrayList

class CurationListPresenter(private val view: CurationListView,
                            private val rssRepository: RssRepository,
                            private val curationRepository: CurationRepository,
                            private val unreadCountRepository: UnreadCountRepository) {
    private var allCurations: ArrayList<Curation> = arrayListOf()


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

    suspend fun activityCreated() = coroutineScope {
        if (rssRepository.getNumOfRss() == 0) {
            view.setNoRssTextToEmptyView()
        }
    }

    suspend fun getView(curation: Curation?, item: CurationItem) {
        curation?.let {
            item.setName(it.name)
            item.setCount(unreadCountRepository.getCurationCount(it.id).toString())
        }
    }
}
