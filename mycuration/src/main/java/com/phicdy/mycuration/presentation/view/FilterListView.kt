package com.phicdy.mycuration.presentation.view

import com.phicdy.mycuration.data.filter.Filter

import java.util.ArrayList

interface FilterListView {
    fun remove(position: Int)
    fun notifyListChanged()
    fun startEditActivity(filterId: Int)
    fun initList(filters: ArrayList<Filter>)
}
