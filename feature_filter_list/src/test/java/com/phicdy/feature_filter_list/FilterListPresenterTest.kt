package com.phicdy.feature_filter_list

import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.entity.Filter
import com.phicdy.mycuration.filterlist.FilterListPresenter
import com.phicdy.mycuration.filterlist.FilterListView
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FilterListPresenterTest {

    private lateinit var presenter: FilterListPresenter
    private lateinit var view: FilterListView
    private lateinit var filterRepository: FilterRepository

    @Before
    fun setup() {
        view = mock()
        filterRepository = mock()
        presenter = FilterListPresenter(view, mock(), filterRepository)
    }

    @Test
    fun `when filter is empty then show empty view`() = runBlocking {
        whenever(filterRepository.getAllFilters()).thenReturn(arrayListOf())
        presenter.resume()
        verify(view, times(1)).hideFilterList()
        verify(view, times(1)).showEmptyView()
    }

    @Test
    fun `when filter is not empty then show filter list`() = runBlocking {
        val filters = arrayListOf(mock<Filter>())
        whenever(filterRepository.getAllFilters()).thenReturn(filters)
        presenter.resume()
        verify(view, times(1)).hideEmptyView()
        verify(view, times(1)).showFilterList(filters)
    }

    @Test
    fun `when delete invalid position then not delete the filter`() = runBlocking {
        val testFilter1 = Filter(1, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onDeleteMenuClicked(-1, testFilter1, 1)
        verify(view, times(0)).remove(-1)
    }

    @Test
    fun `when delete first filter of one then the size is decreased and show empty view`() = runBlocking {
        val testFilter1 = Filter(1, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onDeleteMenuClicked(0, testFilter1, 1)
        verify(view, times(1)).remove(0)
        verify(view, times(1)).hideFilterList()
        verify(view, times(1)).showEmptyView()
    }

    @Test
    fun `when delete first filter of two then the size is decreased and not show empty view`() = runBlocking {
        val testFilter1 = Filter(1, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onDeleteMenuClicked(0, testFilter1, 2)
        verify(view, times(1)).remove(0)
        verify(view, never()).hideFilterList()
        verify(view, never()).showEmptyView()
    }

    @Test
    fun `when edit invalid ID filter then edit activity does not start`() {
        val invalidFilter = Filter(0, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onEditMenuClicked(invalidFilter)
        verify(view, times(0)).startEditActivity(invalidFilter.id)
    }

    @Test
    fun `when edit valid ID filter then edit activity starts`() {
        val validFilter = Filter(1, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onEditMenuClicked(validFilter)
        verify(view, times(1)).startEditActivity(validFilter.id)
    }

    @Test
    fun `when check the filter then the filter is enabled`() = runBlocking {
        val validFilter = Filter(1, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onFilterCheckClicked(validFilter, true)
        assertThat(validFilter.isEnabled).isTrue()
        return@runBlocking
    }

    @Test
    fun `when uncheck the filter then the filter is disabled`() = runBlocking {
        val validFilter = Filter(1, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onFilterCheckClicked(validFilter, false)
        assertThat(validFilter.isEnabled).isFalse()
        return@runBlocking
    }
}
