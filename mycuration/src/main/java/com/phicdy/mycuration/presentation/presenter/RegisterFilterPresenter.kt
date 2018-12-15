package com.phicdy.mycuration.presentation.presenter

import android.view.MenuItem
import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.presentation.view.RegisterFilterView
import kotlinx.coroutines.coroutineScope
import java.util.ArrayList

class RegisterFilterPresenter(private val view: RegisterFilterView,
                              private val dbAdapter: DatabaseAdapter,
                              private val filterRepository: FilterRepository,
                              private val editFilterId: Int = NEW_FILTER_ID) {

    private var selectedFeedList = ArrayList<Feed>()

    private var isEdit = false

    fun setSelectedFeedList(list: ArrayList<Feed>) {
        selectedFeedList = list
        setTargetRssTitle(selectedFeedList)
    }

    fun selectedFeedList(): ArrayList<Feed> {
        return selectedFeedList
    }

    private fun setTargetRssTitle(feeds: ArrayList<Feed>) {
        when {
            feeds.size == 0 -> view.resetFilterTargetRss()
            feeds.size == 1 -> view.setFilterTargetRss(feeds[0].title)
            else -> view.setMultipleFilterTargetRss()
        }
    }

    suspend fun create() = coroutineScope {
        isEdit = editFilterId != NEW_FILTER_ID
        if (isEdit) {
            filterRepository.getFilterById(editFilterId)?.let {
                view.setFilterTitle(it.title)
                view.setFilterUrl(it.url)
                view.setFilterKeyword(it.keyword)
                setSelectedFeedList(it.feeds)
            }
        }
    }

    private fun addMenuClicked() {
        val keywordText = view.filterKeyword()
        val filterUrlText = view.filterUrl()
        val titleText = view.filterTitle()

        //Check title and etKeyword or filter URL has the text
        when {
            titleText.isBlank() -> view.handleEmptyTitle()
            keywordText.isBlank() && filterUrlText.isBlank() -> view.handleEmptyCondition()
            keywordText == "%" || filterUrlText == "%" -> view.handlePercentOnly()
            else -> {
                val result: Boolean
                if (isEdit) {
                    result = dbAdapter.updateFilter(editFilterId, titleText, keywordText, filterUrlText, selectedFeedList)
                    view.trackEdit()
                } else {
                    // Add new filter
                    result = dbAdapter.saveNewFilter(titleText, selectedFeedList, keywordText, filterUrlText)
                    view.trackRegister()
                }
                if (result) {
                    view.showSaveSuccessToast()
                } else {
                    view.showSaveErrorToast()
                }
                view.finish()
            }
        }
    }

    fun optionItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.add_filter -> addMenuClicked()
        }
    }

    companion object {
        private const val NEW_FILTER_ID = -1
    }
}
