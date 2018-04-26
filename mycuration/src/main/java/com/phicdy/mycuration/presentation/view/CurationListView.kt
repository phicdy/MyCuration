package com.phicdy.mycuration.presentation.view

import com.phicdy.mycuration.data.rss.Curation

import java.util.ArrayList

interface CurationListView {
    fun startEditCurationActivity(editCurationId: Int)
    fun setNoRssTextToEmptyView()
    fun setEmptyViewToList()
    fun registerContextMenu()
    fun initListBy(curations: ArrayList<Curation>)
    fun delete(curation: Curation)
    fun size(): Int
    fun curationAt(position: Int): Curation
}
