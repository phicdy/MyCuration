package com.phicdy.mycuration.rss

import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


class RssListFragmentPresenterTest {

    private lateinit var mockView: RssListFragment
    private lateinit var mockArticleRepository: ArticleRepository
    private lateinit var mockRssRepository: RssRepository
    private lateinit var presenter: RssListFragmentPresenter

    @Before
    fun setup() {
        mockView = mock()
        mockArticleRepository = mock()
        mockRssRepository = mock()
        presenter = RssListFragmentPresenter(mockView, mockRssRepository)
    }

    @Test
    fun `when delete ok button is clicked and fails then show error toast`() = runBlocking {
        whenever(mockRssRepository.deleteRss(anyInt())).thenReturn(false)
        presenter.onDeleteOkButtonClicked(0)
        verify(mockView, times(1)).showDeleteFailToast()
    }
}