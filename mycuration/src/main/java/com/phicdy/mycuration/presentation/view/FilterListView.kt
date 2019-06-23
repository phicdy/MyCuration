package com.phicdy.mycuration.presentation.view

import com.phicdy.mycuration.domain.entity.Filter

import java.util.ArrayList

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
