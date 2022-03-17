package com.phicdy.mycuration.top

import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


class TopActivityPresenterTest {

    private lateinit var mockView: TopActivityView
    private lateinit var mockArticleRepository: ArticleRepository
    private lateinit var mockRssRepository: RssRepository
    private lateinit var presenter: TopActivityPresenter

    @Before
    fun setup() {
        mockView = mock()
        mockArticleRepository = mock()
        mockRssRepository = mock()
        presenter = TopActivityPresenter(mockView, mockRssRepository)
    }

    @Test
    fun `when edit ok button is clicked and new title is empty then show error toast`() = runBlocking {
        presenter.onEditFeedOkButtonClicked("", 0)
        verify(mockView, times(1)).showEditFeedTitleEmptyErrorToast()
    }

    @Test
    fun `when edit ok button is clicked and new title is blank then show error toast`() = runBlocking {
        presenter.onEditFeedOkButtonClicked("   ", 0)
        verify(mockView, times(1)).showEditFeedTitleEmptyErrorToast()
    }

    @Test
    fun `when edit ok button is clicked and succeeds then show success toast`() = runBlocking {
        whenever(mockRssRepository.saveNewTitle(anyInt(), anyString())).thenReturn(1)
        presenter.onEditFeedOkButtonClicked("newTitle", 0)
        verify(mockView, times(1)).showEditFeedSuccessToast()
    }

    @Test
    fun `when edit ok button is clicked and fails then show error toast`() = runBlocking {
        whenever(mockRssRepository.saveNewTitle(anyInt(), anyString())).thenReturn(0)
        presenter.onEditFeedOkButtonClicked("newTitle", 0)
        verify(mockView, times(1)).showEditFeedFailToast()
    }

    @Test
    fun `when delete ok button is clicked and fails then show error toast`() = runBlocking {
        whenever(mockRssRepository.deleteRss(anyInt())).thenReturn(false)
        presenter.onDeleteOkButtonClicked(0)
        verify(mockView, times(1)).showDeleteFailToast()
    }
}