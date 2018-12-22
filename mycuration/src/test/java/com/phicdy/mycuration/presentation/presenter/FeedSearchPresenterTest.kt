package com.phicdy.mycuration.presentation.presenter

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.domain.rss.RssParseExecutor
import com.phicdy.mycuration.domain.rss.RssParseResult
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.presentation.view.FeedSearchView
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class FeedSearchPresenterTest {

    private lateinit var networkTaskManager: NetworkTaskManager
    private lateinit var parser: RssParser
    private lateinit var executor: RssParseExecutor
    private lateinit var presenter: FeedSearchPresenter
    private val rssRepository = mock<RssRepository>()
    private lateinit var view: FeedSearchView

    @Before
    fun setup() {
        networkTaskManager = mock()
        parser = mock()
        executor = mock()
        view = mock()
        presenter = FeedSearchPresenter(view, networkTaskManager, rssRepository, executor)

    }

    @Test
    fun testOnCreate() {
        // For coverage
        presenter.create()
    }

    @Test
    fun testOnResume() {
        // For coverage
        presenter.resume()
    }

    @Test
    fun `when fab is clicked in empty then feed hook activity does not show`() {
        presenter.onFabClicked("")
        verify(view, times(0)).startFeedUrlHookActivity("")
    }

    @Test
    fun `when fab is clicked with url then feed hook activity shows`() {
        presenter.onFabClicked("http://www.google.com")
        verify(view, times(1)).startFeedUrlHookActivity("http://www.google.com")
    }

    @Test
    fun `when handle url then progress dialog shows`() = runBlocking {
        presenter.handle("http://www.google.com")
        verify(view, times(1)).showProgressBar()
    }

    @Test
    fun `when handle url then the url is not Loaded in web view`() = runBlocking {
        presenter.handle("http://www.google.com")
        verify(view, times(0)).load("http://www.google.com")
    }

    @Test
    fun `when handle not url then google search is executed in web view`() = runBlocking {
        presenter.handle("abc")
        val url = "https://www.google.co.jp/search?q=abc"
        verify(view, times(1)).load(url)
    }

    @Test
    fun `when handle not url Japanese then google search is executed in web view`() = runBlocking {
        presenter.handle("あいうえお")
        val url = "https://www.google.co.jp/search?q=%E3%81%82%E3%81%84%E3%81%86%E3%81%88%E3%81%8A"
        verify(view, times(1)).load(url)
    }

    @Test
    fun `when handle empty google search is executed in web view`() = runBlocking {
        presenter.handle("")
        val url = "https://www.google.co.jp/search?q="
        verify(view, times(1)).load(url)
    }

    @Test
    fun `when handle not url then progress dialog does not show`() = runBlocking {
        presenter.handle("abc")
        verify(view, times(0)).showProgressBar()
    }

    @Test
    fun `when new feed is added then success toast shows`() = runBlocking {
        // Mock test feed returns
        val testUrl = "http://www.google.com"
        val testFeed = Feed(1, "hoge", testUrl, "", "", 0, "")
        whenever(rssRepository.getFeedByUrl(testUrl)).thenReturn(testFeed)
        val presenter = FeedSearchPresenter(view,
                networkTaskManager, rssRepository, executor)

        presenter.callback.succeeded(testUrl)
        verify(view, times(1)).showAddFeedSuccessToast()
    }

    @Test
    fun `when new feed is added then progress dialog dismisses`() = runBlocking {
        // Mock test feed returns
        val testUrl = "http://www.google.com"
        val testFeed = Feed(1, "hoge", testUrl, "", "", 0, "")
        whenever(rssRepository.getFeedByUrl(testUrl)).thenReturn(testFeed)
        val view = mock<FeedSearchView>()
        val presenter = FeedSearchPresenter(view, networkTaskManager, rssRepository, executor)

        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.NOT_FAILED)
        verify(view, times(1)).dismissProgressBar()
    }

    @Test
    fun `when new feed is added then view finishes`() = runBlocking {
        // Mock test feed returns
        val testUrl = "http://www.google.com"
        val testFeed = Feed(1, "hoge", testUrl, "", "", 0, "")
        whenever(rssRepository.getFeedByUrl(testUrl)).thenReturn(testFeed)
        val presenter = FeedSearchPresenter(view, networkTaskManager, rssRepository, executor)

        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.NOT_FAILED)
        verify(view, times(1)).finishView()
    }

    @Test
    fun `when invalid url comes then toast shows`() {
        val testUrl = "http://hogeagj.com"
        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.INVALID_URL)
        verify(view, times(1)).showInvalidUrlErrorToast()
    }

    @Test
    fun `when not html error occurs then toast shows`() {
        val testUrl = "http://hogeagj.com"
        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.NON_RSS_HTML)
        verify(view, times(1)).showGenericErrorToast()
    }

    @Test
    fun `when not found error occurs then toast shows`() {
        presenter.create()
        presenter.resume()
        val testUrl = "http://hogeagj.com"
        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.NOT_FOUND)
        verify(view, times(1)).showGenericErrorToast()
    }

    @Test
    fun `when new feed is not saved then toast shows`() = runBlocking {
        // Mock null returns
        val testUrl = "http://www.google.com"
        whenever(rssRepository.getFeedByUrl(testUrl)).thenReturn(null)
        val presenter = FeedSearchPresenter(view, networkTaskManager, rssRepository, executor)

        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.NOT_FOUND)
        verify(view, times(1)).showGenericErrorToast()
    }

    @Test
    fun `when search Google then search url is set`() = runBlocking {
        presenter.handle("hoge")
        verify(view, times(1)).setSearchViewTextFrom("https://www.google.co.jp/search?q=hoge")
    }

    @Test
    fun `when failed then the url will be tracked`() {
        val testUrl = "http://hogeagj.com"
        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.INVALID_URL)
        verify(view, times(1)).trackFailedUrl(testUrl)
    }
}
