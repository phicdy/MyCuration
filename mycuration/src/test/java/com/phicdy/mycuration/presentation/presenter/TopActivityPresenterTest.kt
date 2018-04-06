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

    @Before
    fun setup() {
        networkTaskManager = NetworkTaskManager
        mockView = Mockito.mock(TopActivityView::class.java)
        mockAdapter = Mockito.mock(DatabaseAdapter::class.java)
    }

    @Test
    fun `changeTab is called when onCreate`() {
        val presenter = TopActivityPresenter(0, mockView, mockAdapter)
        presenter.create()
        Mockito.verify(mockView, times(1)).changeTab(0)
    }

    @Test
    fun `initViewPager is called when onCreate`() {
        val presenter = TopActivityPresenter(0, mockView, mockAdapter)
        presenter.create()
        Mockito.verify(mockView, times(1)).initViewPager()
    }


    @Test
    fun `setAlarmManager is called when onCreate`() {
        val presenter = TopActivityPresenter(0, mockView, mockAdapter)
        presenter.create()
        Mockito.verify(mockView, times(1)).setAlarmManager()
    }
}