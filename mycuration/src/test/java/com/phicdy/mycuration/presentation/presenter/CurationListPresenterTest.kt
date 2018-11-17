package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.data.rss.Curation
import com.phicdy.mycuration.presentation.view.CurationItem
import com.phicdy.mycuration.presentation.view.CurationListView
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.util.ArrayList

class CurationListPresenterTest {

    private val adapter = mock(DatabaseAdapter::class.java)
    private val view = mock(CurationListView::class.java)
    private lateinit var presenter: CurationListPresenter
    private val item = mock(CurationItem::class.java)
    private val repository = mock(UnreadCountRepository::class.java)

    @Before
    fun setUp() {
        DatabaseAdapter.inject(adapter)
        presenter = CurationListPresenter(view, adapter, repository)
    }

    @Test
    fun testOnCreate() {
        // For coverage
        presenter.create()
    }

    @Test
    fun `when after onResume then list is set`() {
        val curations = ArrayList<Curation>()
        val testName = "testCuration"
        curations.add(Curation(1, testName))
        `when`(adapter.allCurations).thenReturn(curations)
        presenter.resume()
        verify(view, times(1)).initListBy(curations)
        verify(view, never()).showEmptyView()
        verify(view, times(1)).showRecyclerView()
    }

    @Test
    fun testOnPause() {
        // For coverage
        presenter.pause()
    }

    @Test
    fun `when edit is clicked then edit activity starts`() {
        presenter.onCurationEditClicked(1)
        verify(view, times(1)).startEditCurationActivity(1)
    }

    @Test
    fun `when edit is clicked then invalid curation does not affect`() {
        presenter.create()
        presenter.resume()
        presenter.onCurationEditClicked(-1)
        verify(view, never()).startEditCurationActivity(1)
    }

    @Test
    fun `when empty rss then show empty view`() {
        `when`(adapter.numOfFeeds).thenReturn(0)
        presenter.resume()
        verify(view, never()).showRecyclerView()
        verify(view, times(1)).hideRecyclerView()
        verify(view, times(1)).showEmptyView()
    }

    @Test
    fun `when delete is clicked then curation is deleted and hide the list`() {
        val curation = Curation(1, "test")
        val curations = arrayListOf<Curation>().apply {
            add(curation)
        }
        `when`(adapter.allCurations).thenReturn(curations)
        presenter.onCurationDeleteClicked(curation, curations.size)
        verify(adapter, times(1)).deleteCuration(curation.id)
        verify(view, times(1)).hideRecyclerView()
        verify(view, times(1)).showEmptyView()
    }

    @Test
    fun `when delete is clicked in two curations then curation is deleted and not hide the list`() {
        val curation = Curation(1, "test")
        val curations = arrayListOf<Curation>().apply {
            add(curation)
            add(Curation(2, "test2"))
        }
        `when`(adapter.allCurations).thenReturn(curations)
        presenter.onCurationDeleteClicked(curation, curations.size)
        verify(adapter, times(1)).deleteCuration(curation.id)
        verify(view, never()).hideRecyclerView()
        verify(view, never()).showEmptyView()
    }

    @Test
    fun `when rss is empty then no rss view is set`() {
        `when`(adapter.numOfFeeds).thenReturn(0)
        presenter.activityCreated()
        verify(view, times(1)).setNoRssTextToEmptyView()
    }

    @Test
    fun `when curation is null then not set name and count`() = runBlocking {
        presenter.getView(null, item)
        verify(item, never()).setName("")
        verify(item, never()).setCount("")
    }

    @Test
    fun `when curation is not null then not set name and count`() = runBlocking {
        `when`(repository.getCurationCount(1)).thenReturn(10)
        presenter.getView(Curation(1, "name"), item)
        verify(item, times(1)).setName("name")
        verify(item, times(1)).setCount("10")
    }
}
