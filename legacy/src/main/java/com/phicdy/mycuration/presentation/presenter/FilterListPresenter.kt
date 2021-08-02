package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Filter
import com.phicdy.mycuration.presentation.view.FilterListView
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class FilterListPresenter @Inject constructor(
        private val view: FilterListView,
        private val rssRepository: RssRepository,
        private val filterRepository: FilterRepository
) {

    fun onActivityCreated() {
        val num = rssRepository.getNumOfRss()
        if (num == 0L) {
            view.setRssEmptyMessage()
        }
    }

    suspend fun resume() = coroutineScope {
        filterRepository.getAllFilters().let {
            if (it.isEmpty()) {
                view.hideFilterList()
                view.showEmptyView()
            } else {
                view.hideEmptyView()
                view.showFilterList(ArrayList(it))
            }
        }

    }

    suspend fun onDeleteMenuClicked(position: Int, selectedFilter: Filter, currentSize: Int) = coroutineScope {
        if (position < 0) return@coroutineScope
        filterRepository.deleteFilter(selectedFilter.id)
        view.remove(position)
        view.notifyListChanged()
        if (currentSize == 1) {
            view.hideFilterList()
            view.showEmptyView()
        }
    }

    fun onEditMenuClicked(selectedFilter: Filter) {
        val id = selectedFilter.id
        // Database table ID starts with 1, ID under 1 means invalid
        if (id <= 0) return
        view.startEditActivity(id)
    }

    suspend fun onFilterCheckClicked(clickedFilter: Filter, isChecked: Boolean) = coroutineScope {
        clickedFilter.isEnabled = isChecked
        filterRepository.updateEnabled(clickedFilter.id, isChecked)
    }
}
