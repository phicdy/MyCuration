package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.rss.Curation
import com.phicdy.mycuration.domain.rss.UnreadCountManager
import com.phicdy.mycuration.presentation.view.CurationItem
import com.phicdy.mycuration.presentation.view.CurationListView
import java.util.ArrayList

class CurationListPresenter(private val view: CurationListView,
                            private val dbAdapter: DatabaseAdapter) : Presenter {
    private var allCurations: ArrayList<Curation> = arrayListOf()

    override fun create() {}

    override fun resume() {
        view.registerContextMenu()
        allCurations = dbAdapter.allCurations
        view.initListBy(allCurations)
    }

    override fun pause() {}

    fun onCurationEditClicked(curationId: Int) {
        if (curationId < 0) return
        view.startEditCurationActivity(curationId)
    }

    fun onCurationDeleteClicked(curation: Curation) {
        dbAdapter.deleteCuration(curation.id)
        view.delete(curation)
    }

    fun activityCreated() {
        if (dbAdapter.numOfFeeds == 0) {
            view.setNoRssTextToEmptyView()
        }
        view.setEmptyViewToList()
    }

    fun getCurationIdAt(position: Int): Int {
        if (position < 0 || position > view.size()) {
            return -1
        }

        return allCurations[position].id
    }

    fun getView(curation: Curation?, item: CurationItem) {
        curation?.let {
            item.setName(it.name)
            item.setCount(UnreadCountManager.getCurationCount(it.id).toString())
        }
    }
}
