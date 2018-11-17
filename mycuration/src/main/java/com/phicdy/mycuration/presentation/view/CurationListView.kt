package com.phicdy.mycuration.presentation.view

import com.phicdy.mycuration.data.rss.Curation

import java.util.ArrayList

interface CurationListView {
    fun startEditCurationActivity(editCurationId: Int)
    fun setNoRssTextToEmptyView()
    fun registerContextMenu()
    fun initListBy(curations: ArrayList<Curation>)
    fun showRecyclerView()
    fun hideRecyclerView()
    fun showEmptyView()
    fun hideEmptyView()
}
