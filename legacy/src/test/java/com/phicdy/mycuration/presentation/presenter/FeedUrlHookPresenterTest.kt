package com.phicdy.mycuration.presentation.presenter

import android.content.Intent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.rss.RssParseResult
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.presentation.view.FeedUrlHookView
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class FeedUrlHookPresenterTest {

    private val networkTaskManager = mock<NetworkTaskManager>()
    private val rssRepository = mock<RssRepository>()
    private lateinit var parser: RssParser
    private lateinit var view: FeedUrlHookView

    @Before
    fun setup() {
        parser = mock()
        view = mock()
    }

    @Test
    fun `when empty action then finish`() = runBlocking {
        val presenter = FeedUrlHookPresenter(view, "", "", "", rssRepository,
                networkTaskManager, this, parser)
        presenter.create()
        verify(view, times(1)).finishView()
    }

    @Test
    fun `when invalid action comes then finish`() = runBlocking {
        val presenter = FeedUrlHookPresenter(view, "hogehoge", "http://www.google.com", "",
                rssRepository, networkTaskManager, this, parser)
        presenter.create()
        verify(view, times(1)).finishView()
    }

    @Test
    fun `when action view and invalid url comes then toast shows`() = runBlocking {
        val presenter = FeedUrlHookPresenter(view, Intent.ACTION_VIEW, "hogehoge", "",
                rssRepository, networkTaskManager, this, parser)
        presenter.create()
        verify(view, times(1)).showInvalidUrlErrorToast()
    }

    @Test
    fun `when action send and invalid url comes then toast shows`() = runBlocking {
        val presenter = FeedUrlHookPresenter(view, Intent.ACTION_SEND, "", "hogehoge",
                rssRepository, networkTaskManager, this, parser)
        presenter.create()
        verify(view, times(1)).showInvalidUrlErrorToast()
    }

    @Test
    fun `when finish add feed action comes then view finishes`() = runBlocking {
        val presenter = FeedUrlHookPresenter(view, "", "", "", rssRepository,
                networkTaskManager, this, parser)
        presenter.callback.failed(RssParseResult.FailedReason.NOT_FOUND, "")
        verify(view, times(1)).finishView()
    }

    @Test
    fun `when finish add feed action comes with not RSS html error then toast shows`() = runBlocking {
        val presenter = FeedUrlHookPresenter(view, "", "", "", rssRepository,
                networkTaskManager, this, parser)
        presenter.create()
        presenter.callback.failed(RssParseResult.FailedReason.NON_RSS_HTML, "")
        verify(view, times(1)).showGenericErrorToast()
    }

    @Test
    fun `when finish add feed action comes with invalid url error then toast shows`() = runBlocking {
        val presenter = FeedUrlHookPresenter(view, "", "", "", rssRepository,
                networkTaskManager, this, parser)
        presenter.create()
        presenter.callback.failed(RssParseResult.FailedReason.INVALID_URL, "")
        verify(view, times(1)).showInvalidUrlErrorToast()
    }

    @Test
    fun `when failed then the url will be tracked`() = runBlocking {
        val presenter = FeedUrlHookPresenter(view, "", "", "", rssRepository,
                networkTaskManager, this, parser)
        presenter.create()
        val failUrl = "http://www.google.com"
        presenter.callback.failed(RssParseResult.FailedReason.INVALID_URL, failUrl)
        verify(view, times(1)).trackFailedUrl(failUrl)
    }
}
