package com.phicdy.mycuration.presentation.presenter

import android.content.Intent

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.domain.rss.RssParseResult
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.domain.rss.UnreadCountManager
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.presentation.view.FeedUrlHookView

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import junit.framework.Assert.assertTrue
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class FeedUrlHookPresenterTest {

    private lateinit var networkTaskManager: NetworkTaskManager
    private lateinit var adapter: DatabaseAdapter
    private lateinit var presenter: FeedUrlHookPresenter
    private lateinit var parser: RssParser
    private lateinit var view: FeedUrlHookView

    @Before
    fun setup() {
        networkTaskManager = NetworkTaskManager
        adapter = Mockito.mock(DatabaseAdapter::class.java)
        DatabaseAdapter.inject(Mockito.mock(DatabaseAdapter::class.java))
        parser = Mockito.mock(RssParser::class.java)
        view = Mockito.mock(FeedUrlHookView::class.java)
        presenter = FeedUrlHookPresenter(view, "", "", "",
                adapter, UnreadCountManager, networkTaskManager, parser)
    }

    @Test
    fun `when empty action then finish`() {
        presenter.create()
        verify(view, times(1)).finishView()
    }

    @Test
    fun testOnResume() {
        // For coverage
        presenter.resume()
        assertTrue(true)
    }

    @Test
    fun `when invalid action comes then finish`() {
        presenter = FeedUrlHookPresenter(view, "hogehoge", "http://www.google.com", "",
                adapter, UnreadCountManager, networkTaskManager, parser)
        presenter.create()
        verify(view, times(1)).finishView()
    }

    @Test
    fun `when action view and invalid url comes then toast shows`() {
        presenter = FeedUrlHookPresenter(view, Intent.ACTION_VIEW, "hogehoge", "",
                adapter, UnreadCountManager, networkTaskManager, parser)
        presenter.create()
        verify(view, times(1)).showInvalidUrlErrorToast()
    }

    @Test
    fun `when action send and invalid url comes then toast shows`() {
        presenter = FeedUrlHookPresenter(view, Intent.ACTION_SEND, "", "hogehoge",
                adapter, UnreadCountManager, networkTaskManager, parser)
        presenter.create()
        verify(view, times(1)).showInvalidUrlErrorToast()
    }

    @Test
    fun `when finish add feed action comes then view finishes`() {
        presenter.callback.failed(RssParseResult.FailedReason.NOT_FOUND, "")
        verify(view, times(1)).finishView()
    }

    @Test
    fun `when finish add feed action comes with not RSS html error then toast shows`() {
        presenter.create()
        presenter.callback.failed(RssParseResult.FailedReason.NON_RSS_HTML, "")
        verify(view, times(1)).showGenericErrorToast()
    }

    @Test
    fun `when finish add feed action comes with invalid url error then toast shows`() {
        presenter.create()
        presenter.callback.failed(RssParseResult.FailedReason.INVALID_URL, "")
        verify(view, times(1)).showInvalidUrlErrorToast()
    }

    @Test
    fun `when failed then the url will be tracked`() {
        presenter.create()
        val failUrl = "http://www.google.com"
        presenter.callback.failed(RssParseResult.FailedReason.INVALID_URL, failUrl)
        verify(view, times(1)).trackFailedUrl(failUrl)
    }
}
