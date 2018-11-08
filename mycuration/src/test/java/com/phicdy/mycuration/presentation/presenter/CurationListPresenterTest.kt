package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.data.rss.Curation
import com.phicdy.mycuration.presentation.view.CurationItem
import com.phicdy.mycuration.presentation.view.CurationListView
import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
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
    private val view = MockView()
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
        presenter.create()
        presenter.resume()
        assertThat(view.curations[0].name, `is`(testName))
    }

    @Test
    fun testOnPause() {
        // For coverage
        presenter.pause()
    }

    @Test
    fun `when edit is clicked then edit activity starts`() {
        presenter.create()
        presenter.resume()
        presenter.onCurationEditClicked(1)
        assertThat(view.startedEditCurationId, `is`(1))
    }

    @Test
    fun `when edit is clicked then invalid curation does not affect`() {
        presenter.create()
        presenter.resume()
        presenter.onCurationEditClicked(-1)
        assertThat<Int>(view.startedEditCurationId, `is`<Int>(MockView.DEFAULT_EDIT_ID))
    }

    @Test
    fun `when empty rss then empty view is set`() {
        `when`(adapter.numOfFeeds).thenReturn(0)
        presenter.create()
        presenter.resume()
        presenter.activityCreated()
        assertThat<Int>(view.startedEditCurationId, `is`<Int>(MockView.DEFAULT_EDIT_ID))
    }

    @Test
    fun `when delete is clicked then curation is feleted`() {
        val curations = ArrayList<Curation>()
        val curation = Curation(1, "test")
        curations.add(curation)
        `when`(adapter.allCurations).thenReturn(curations)
        presenter.create()
        presenter.resume()
        presenter.onCurationDeleteClicked(curation)
        assertThat(view.curations.size, `is`(0))
    }

    @Test
    fun `when rss is empty then no rss view is set`() {
        `when`(adapter.numOfFeeds).thenReturn(0)
        presenter.create()
        presenter.resume()
        presenter.activityCreated()
        assertTrue(view.isNoRssViewSet)
    }

    @Test
    fun `when rss is empty then empty view is set`() {
        `when`(adapter.numOfFeeds).thenReturn(0)
        presenter.create()
        presenter.resume()
        presenter.activityCreated()
        assertTrue(view.isEmptyViewToList)
    }

    @Test
    fun `when get curation ID that under 0 position then returns -1`() {
        presenter.create()
        presenter.resume()
        assertThat(presenter.getCurationIdAt(-1), `is`(-1))
    }

    @Test
    fun `when get curation ID that bigger index than view size then returns -1`() {
        val curations = ArrayList<Curation>()
        val testName = "testCuration"
        curations.add(Curation(1, testName))
        `when`(adapter.allCurations).thenReturn(curations)
        presenter.create()
        presenter.resume()
        assertThat(presenter.getCurationIdAt(curations.size), `is`(-1))
    }

    @Test
    fun `when get curation ID then return curation of index`() {
        val curations = ArrayList<Curation>()
        val testId = 1
        curations.add(Curation(testId, "testName"))
        `when`(adapter.allCurations).thenReturn(curations)
        presenter.create()
        presenter.resume()
        assertThat(presenter.getCurationIdAt(0), `is`(testId))
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

    private class MockView : CurationListView {
        var curations: ArrayList<Curation> = arrayListOf()
        var startedEditCurationId = DEFAULT_EDIT_ID
        var isNoRssViewSet = false
        var isEmptyViewToList = false

        override fun startEditCurationActivity(editCurationId: Int) {
            startedEditCurationId = editCurationId
        }

        override fun setNoRssTextToEmptyView() {
            isNoRssViewSet = true
        }

        override fun setEmptyViewToList() {
            isEmptyViewToList = true
        }

        override fun registerContextMenu() {

        }

        override fun initListBy(curations: ArrayList<Curation>) {
            this.curations = curations
        }

        override fun delete(curation: Curation) {
            curations.remove(curation)
        }

        override fun size(): Int {
            return 0
        }

        companion object {
            const val DEFAULT_EDIT_ID = -1000
        }
    }
}
