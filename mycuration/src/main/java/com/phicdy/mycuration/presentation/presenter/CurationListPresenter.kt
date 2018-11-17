package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.data.rss.Curation
import com.phicdy.mycuration.presentation.view.CurationItem
import com.phicdy.mycuration.presentation.view.CurationListView
import java.util.ArrayList

class CurationListPresenter(private val view: CurationListView,
                            private val dbAdapter: DatabaseAdapter,
                            private val unreadCountRepository: UnreadCountRepository) : Presenter {
    private var allCurations: ArrayList<Curation> = arrayListOf()

    override fun create() {}

    override fun resume() {
        view.registerContextMenu()
        allCurations = dbAdapter.allCurations
        if (allCurations.isEmpty()) {
            view.hideRecyclerView()
            view.showEmptyView()
        } else {
            view.hideEmptyView()
            view.showRecyclerView()
            view.initListBy(allCurations)
        }
    }

    override fun pause() {}

    fun onCurationEditClicked(curationId: Int) {
        if (curationId < 0) return
        view.startEditCurationActivity(curationId)
    }

    fun onCurationDeleteClicked(curation: Curation, size: Int) {
        dbAdapter.deleteCuration(curation.id)
        if (size == 1) {
            view.hideRecyclerView()
            view.showEmptyView()
        }
    }

    fun activityCreated() {
        if (dbAdapter.numOfFeeds == 0) {
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
