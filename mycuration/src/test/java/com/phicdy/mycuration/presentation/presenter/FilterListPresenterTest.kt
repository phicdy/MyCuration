package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.filter.Filter
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.presentation.view.FilterListView
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.util.ArrayList

class FilterListPresenterTest {

    private val adapter = Mockito.mock(DatabaseAdapter::class.java)
    private lateinit var presenter: FilterListPresenter
    private lateinit var view: FilterListView
    private lateinit var filterRepository: FilterRepository

    @Before
    fun setup() {
        view = Mockito.mock(FilterListView::class.java)
        filterRepository = mock(FilterRepository::class.java)
        presenter = FilterListPresenter(view, mock(RssRepository::class.java), filterRepository, adapter)
    }

    @Test
    fun `when filter is empty then show empty view`() = runBlocking {
        `when`(filterRepository.getAllFilters()).thenReturn(arrayListOf())
        presenter.resume()
        verify(view, times(1)).hideFilterList()
        verify(view, times(1)).showEmptyView()
    }

    @Test
    fun `when filter is not empty then show filter list`() = runBlocking {
        val filters = arrayListOf(mock(Filter::class.java))
        `when`(filterRepository.getAllFilters()).thenReturn(filters)
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
    fun `when check the filter then the filter is enabled`() {
        val validFilter = Filter(1, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onFilterCheckClicked(validFilter, true)
        assertTrue(validFilter.isEnabled)
    }

    @Test
    fun `when uncheck the filter then the filter is disabled`() {
        val validFilter = Filter(1, "test1", "testKeyword1", "http://test1.com", ArrayList(), -1, Filter.TRUE)
        presenter.onFilterCheckClicked(validFilter, false)
        assertFalse(validFilter.isEnabled)
    }
}
