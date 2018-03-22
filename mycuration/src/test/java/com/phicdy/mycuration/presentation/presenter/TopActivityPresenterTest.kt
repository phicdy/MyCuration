package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.task.NetworkTaskManager
import com.phicdy.mycuration.presentation.view.TopActivityView
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.times


class TopActivityPresenterTest {

    private lateinit var networkTaskManager: NetworkTaskManager
    private lateinit var mockView: TopActivityView

    @Before
    fun setup() {
        networkTaskManager = NetworkTaskManager
        mockView = Mockito.mock(TopActivityView::class.java)
    }

    @Test
    fun `changeTab is called when onCreate`() {
        val presenter = TopActivityPresenter(0)
        presenter.setView(mockView)
        presenter.create()
        Mockito.verify(mockView, times(1)).changeTab(0)
    }

    @Test
    fun `initViewPager is called when onCreate`() {
        val presenter = TopActivityPresenter(0)
        presenter.setView(mockView)
        presenter.create()
        Mockito.verify(mockView, times(1)).initViewPager()
    }


    @Test
    fun `setAlarmManager is called when onCreate`() {
        val presenter = TopActivityPresenter(0)
        presenter.setView(mockView)
        presenter.create()
        Mockito.verify(mockView, times(1)).setAlarmManager()
    }
}