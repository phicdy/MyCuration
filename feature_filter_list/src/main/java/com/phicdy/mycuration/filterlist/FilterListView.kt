package com.phicdy.mycuration.filterlist

import com.phicdy.mycuration.entity.Filter

interface FilterListView {
    fun remove(position: Int)
    fun notifyListChanged()
    fun startEditActivity(filterId: Int)
    fun showFilterList(filters: ArrayList<Filter>)
    fun hideFilterList()
    fun showEmptyView()
    fun hideEmptyView()
    fun setRssEmptyMessage()
}
