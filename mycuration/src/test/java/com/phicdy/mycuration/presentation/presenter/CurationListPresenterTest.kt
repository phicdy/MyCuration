package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.data.rss.Curation
import com.phicdy.mycuration.presentation.view.CurationItem
import com.phicdy.mycuration.presentation.view.CurationListView
import kotlinx.coroutines.runBlocking
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
    private val curationRepository = mock(CurationRepository::class.java)
    private val rssRepository = mock(RssRepository::class.java)

    @Before
    fun setUp() {
        DatabaseAdapter.inject(adapter)
        presenter = CurationListPresenter(view, rssRepository, curationRepository, repository)
    }

    @Test
    fun `when after onResume then list is set`() = runBlocking {
        val curations = ArrayList<Curation>()
        val testName = "testCuration"
        curations.add(Curation(1, testName))
        `when`(curationRepository.getAllCurations()).thenReturn(curations)
        presenter.resume()
        verify(view, times(1)).initListBy(curations)
        verify(view, never()).showEmptyView()
        verify(view, times(1)).showRecyclerView()
    }

    @Test
    fun `when edit is clicked then edit activity starts`() {
        presenter.onCurationEditClicked(1)
        verify(view, times(1)).startEditCurationActivity(1)
    }

    @Test
    fun `when edit is clicked then invalid curation does not affect`() = runBlocking {
        presenter.onCurationEditClicked(-1)
        verify(view, never()).startEditCurationActivity(-1)
    }

    @Test
    fun `when empty rss then show empty view`() = runBlocking {
        `when`(curationRepository.getAllCurations()).thenReturn(arrayListOf())
        presenter.resume()
        verify(view, never()).showRecyclerView()
        verify(view, times(1)).hideRecyclerView()
        verify(view, times(1)).showEmptyView()
    }

    @Test
    fun `when delete is clicked then curation is deleted and hide the list`() = runBlocking {
        val curation = Curation(1, "test")
        val curations = arrayListOf<Curation>().apply {
            add(curation)
        }
        `when`(curationRepository.getAllCurations()).thenReturn(curations)
        presenter.onCurationDeleteClicked(curation, curations.size)
        verify(curationRepository, times(1)).delete(curation.id)
        verify(view, times(1)).hideRecyclerView()
        verify(view, times(1)).showEmptyView()
    }

    @Test
    fun `when delete is clicked in two curations then curation is deleted and not hide the list`() = runBlocking {
        val curation = Curation(1, "test")
        val curations = arrayListOf<Curation>().apply {
            add(curation)
            add(Curation(2, "test2"))
        }
        `when`(curationRepository.getAllCurations()).thenReturn(curations)
        presenter.onCurationDeleteClicked(curation, curations.size)
        verify(curationRepository, times(1)).delete(curation.id)
        verify(view, never()).hideRecyclerView()
        verify(view, never()).showEmptyView()
    }

    @Test
    fun `when rss is empty then no rss view is set`() = runBlocking {
        `when`(rssRepository.getNumOfRss()).thenReturn(0)
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
