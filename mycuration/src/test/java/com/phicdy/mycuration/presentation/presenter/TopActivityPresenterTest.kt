package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.presentation.view.TopActivityView
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.times


class TopActivityPresenterTest {

    private lateinit var networkTaskManager: NetworkTaskManager
    private lateinit var mockView: TopActivityView
    private lateinit var mockAdapter: DatabaseAdapter
    private lateinit var presenter: TopActivityPresenter

    @Before
    fun setup() {
        networkTaskManager = NetworkTaskManager
        mockView = Mockito.mock(TopActivityView::class.java)
        mockAdapter = Mockito.mock(DatabaseAdapter::class.java)
        presenter = TopActivityPresenter(0, mockView, mockAdapter)
    }

    @Test
    fun `changeTab is called when onCreate`() {
        presenter.create()
        Mockito.verify(mockView, times(1)).changeTab(0)
    }

    @Test
    fun `initViewPager is called when onCreate`() {
        presenter.create()
        Mockito.verify(mockView, times(1)).initViewPager()
    }

    @Test
    fun `initToolbar is called when onCreate`() {
        presenter.create()
        Mockito.verify(mockView, times(1)).initToolbar()
    }

    @Test
    fun `setAlarmManager is called when onCreate`() {
        presenter.create()
        Mockito.verify(mockView, times(1)).setAlarmManager()
    }

    @Test
    fun `not go to artcile search result when query is null`() {
        presenter.queryTextSubmit(null)
        Mockito.verify(mockView, times(0)).goToArticleSearchResult(null.toString())
    }

    @Test
    fun `go to artcile search result when query is not null`() {
        presenter.queryTextSubmit("query")
        Mockito.verify(mockView, times(1)).goToArticleSearchResult("query")
    }
}