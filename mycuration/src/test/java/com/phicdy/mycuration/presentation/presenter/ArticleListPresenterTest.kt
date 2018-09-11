package com.phicdy.mycuration.presentation.presenter

import android.content.Intent
import com.phicdy.mycuration.data.rss.Article
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.domain.rss.UnreadCountManager
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.presentation.view.ArticleListView
import com.phicdy.mycuration.util.PreferenceHelper

import org.junit.Test
import org.mockito.Mockito

import java.util.ArrayList

import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times

class ArticleListPresenterTest {

    @Test
    fun testOnCreate() {
        // For coverage
        val adapter = Mockito.mock(DatabaseAdapter::class.java)
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val presenter = ArticleListPresenter(
                1, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
    }

    @Test
    fun `No articles are loaded after onCreateView with empty DB for feed`() {
        val testFeedId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val adapter = mockEmptyDatabase(testFeedId)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertThat(presenter.articleSize(), `is`(0))
    }

    @Test
    fun `No Articles are loaded after onCreateView with empty DB for all feed`() {
        val testFeedId = Feed.ALL_FEED_ID
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val adapter = mockEmptyDatabase(testFeedId)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertThat(presenter.articleSize(), `is`(0))
    }

    @Test
    fun `No Articles are loaded after onCreateView with empty DB for curation`() {
        val testCurationId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val adapter = mockEmptyDatabase(testCurationId)
        val presenter = ArticleListPresenter(
                Feed.DEFAULT_FEED_ID, testCurationId, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertThat(presenter.articleSize(), `is`(0))
    }

    @Test
    fun `All artciles of feed are loaded after onCreateView if already read`() {
        val testFeedId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val adapter = mock1ReadArticleDatabase(testFeedId)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertThat(presenter.articleSize(), `is`(1))
    }

    @Test
    fun `All artciles are loaded after onCreateView if already read`() {
        val testFeedId = Feed.ALL_FEED_ID
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val adapter = mock1ReadArticleDatabase(testFeedId)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertThat(presenter.articleSize(), `is`(1))
    }

    @Test
    fun `All artciles OfCurationare loaded after onCreateView if already read`() {
        val testCurationId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val adapter = mock1ReadArticleDatabase(testCurationId)
        val presenter = ArticleListPresenter(
                Feed.DEFAULT_FEED_ID, testCurationId, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertThat(presenter.articleSize(), `is`(1))
    }

    @Test
    fun `All unread articles of feed are only loaded after onCreateView`() {
        val testFeedId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val adapter = mock2Unread2ReadArticleDatabase(testFeedId)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertTrue(presenter.isAllUnreadArticle)
    }

    @Test
    fun `All unread articles are only loaded after onCreateView`() {
        val testFeedId = Feed.ALL_FEED_ID
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val adapter = mock2Unread2ReadArticleDatabase(testFeedId)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertTrue(presenter.isAllUnreadArticle)
    }

    @Test
    fun `All unread articles of curation are only loaded after onCreateView`() {
        val testCurationId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val adapter = mock2Unread2ReadArticleDatabase(testCurationId)
        val presenter = ArticleListPresenter(
                Feed.DEFAULT_FEED_ID, testCurationId, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertTrue(presenter.isAllUnreadArticle)
    }

    @Test
    fun testOnPause() {
        // For coverage
        val adapter = Mockito.mock(DatabaseAdapter::class.java)
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val presenter = ArticleListPresenter(
                1, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.pause()
    }

    @Test
    fun `unread article status becomes to read when clicked`() {
        val testFeedId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        val adapter = mock1ArticleDatabase(clickedArticle)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertThat(clickedArticle.status, `is`(Article.TOREAD))
    }

    @Test
    fun `Toread article status is still to read when clicked`() {
        val testFeedId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.TOREAD, "1", 1, 1, "feed", "")
        val adapter = mock1ArticleDatabase(clickedArticle)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertThat(clickedArticle.status, `is`(Article.TOREAD))
    }

    @Test
    fun `Read article status is still read when clicked`() {
        val testFeedId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.READ, "1", 1, 1, "feed", "")
        val adapter = mock1ArticleDatabase(clickedArticle)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertThat(clickedArticle.status, `is`(Article.READ))
    }

    @Test
    fun `Article is opened with internal option when clicked`() {
        val testFeedId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        val adapter = mock1ArticleDatabase(clickedArticle)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertThat<String>(view.openedUrl, `is`(clickedArticle.url))
    }

    @Test
    fun `Article is opened with external option when clicked`() {
        val testFeedId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        val adapter = mock1ArticleDatabase(clickedArticle)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, false, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertThat<String>(view.openedUrl, `is`(clickedArticle.url))
    }

    @Test
    fun `Intenal web view is opened with internal option when clicked`() {
        val testFeedId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        val adapter = mock1ArticleDatabase(clickedArticle)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertTrue(view.isOpenedInternalWebView)
    }

    @Test
    fun `Title of intenal web view is opended article's RSS's title`() {
        val testFeedId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        val adapter = mock1ArticleDatabase(clickedArticle)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertThat(view.openTitle, `is`(clickedArticle.feedTitle))
    }

    @Test
    fun `Title of intenal web view is opened article's RSS's title for all RSS`() {
        val testFeedId = Feed.ALL_FEED_ID
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        val adapter = mock1ArticleDatabase(clickedArticle)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertThat(view.openTitle, `is`(clickedArticle.feedTitle))
    }

    @Test
    fun `Extenal web view is opened with external option when clicked`() {
        val testFeedId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        val adapter = mock1ArticleDatabase(clickedArticle)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, false, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertTrue(view.isOpenedExternalWebView)
    }

    @Test
    fun `Share UI shows when long clicked`() {
        val testFeedId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val longClickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        val adapter = mock1ArticleDatabase(longClickedArticle)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT, "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemLongClicked(0)
        assertThat<String>(view.shareUrl, `is`(longClickedArticle.url))
    }

    @Test
    fun `No search result view shows when search action and no result`() {
        val testFeedId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val article = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        val adapter = mock1ArticleDatabase(article)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT,
                "ghaeogha", Intent.ACTION_SEARCH)
        val view = MockView()
        presenter.setView(view)
        presenter.createView()
        assertTrue(view.isNoSearchResultShowed)
    }

    @Test
    fun `No article view shows when no search action and no result`() {
        val testFeedId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val adapter = mockEmptyDatabase(1)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT,
                "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.createView()
        assertTrue(view.isNoArticleShowed)
    }

    @Test
    fun `Search article when search action`() {
        val testFeedId = 1
        val manager = Mockito.mock(UnreadCountManager::class.java)
        val article = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        val adapter = mock1ArticleDatabase(article)
        val query = "query"
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT,
                query, Intent.ACTION_SEARCH)
        val view = MockView()
        presenter.setView(view)
        presenter.createView()
        Mockito.verify(adapter, times(1)).searchArticles(query, true)

    }

    private fun mockEmptyDatabase(testId: Int): DatabaseAdapter {
        val adapter = Mockito.mock(DatabaseAdapter::class.java)
        `when`(adapter.getUnreadArticlesInAFeed(testId, true)).thenReturn(ArrayList())
        `when`(adapter.isExistArticle(testId)).thenReturn(false)
        `when`(adapter.getAllUnreadArticlesOfCuration(testId, true)).thenReturn(ArrayList())
        `when`(adapter.getAllArticlesOfCuration(testId, true)).thenReturn(ArrayList())
        return adapter
    }

    private fun mock1ReadArticleDatabase(testId: Int): DatabaseAdapter {
        val adapter = Mockito.mock(DatabaseAdapter::class.java)
        `when`(adapter.getUnreadArticlesInAFeed(testId, true)).thenReturn(ArrayList())
        `when`(adapter.getAllUnreadArticlesOfCuration(testId, true)).thenReturn(ArrayList())
        `when`(adapter.isExistArticle(testId)).thenReturn(true)
        `when`(adapter.isExistArticle).thenReturn(true)
        val articles = ArrayList<Article>()
        articles.add(Article(1, "hoge", "http://www.google.com",
                Article.READ, "1", 1, 1, "feed", ""))
        `when`(adapter.getAllArticlesInAFeed(testId, true)).thenReturn(articles)
        `when`(adapter.getTop300Articles(true)).thenReturn(articles)
        `when`(adapter.getAllArticlesOfCuration(testId, true)).thenReturn(articles)
        return adapter
    }

    private fun mock1ArticleDatabase(article: Article): DatabaseAdapter {
        val testId = 1
        val adapter = Mockito.mock(DatabaseAdapter::class.java)
        val articles = ArrayList<Article>()
        articles.add(article)
        `when`(adapter.getAllUnreadArticles(true)).thenReturn(articles)
        `when`(adapter.getTop300Articles(true)).thenReturn(articles)
        `when`(adapter.getUnreadArticlesInAFeed(testId, true)).thenReturn(articles)
        `when`(adapter.isExistArticle(testId)).thenReturn(true)
        val mockFeed = Mockito.mock(Feed::class.java)
        `when`(mockFeed.title).thenReturn("feed")
        `when`(adapter.getFeedById(testId)).thenReturn(mockFeed)
        return adapter
    }

    private fun mock2Unread2ReadArticleDatabase(testId: Int): DatabaseAdapter {
        val adapter = Mockito.mock(DatabaseAdapter::class.java)
        val unreadArticles = ArrayList<Article>()
        unreadArticles.add(Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", ""))
        unreadArticles.add(Article(2, "unread2", "http://www.google.com",
                Article.UNREAD, "1", 1, 2, "feed", ""))
        `when`(adapter.getUnreadArticlesInAFeed(testId, true)).thenReturn(unreadArticles)
        `when`(adapter.getAllUnreadArticlesOfCuration(testId, true)).thenReturn(unreadArticles)
        `when`(adapter.isExistArticle(testId)).thenReturn(true)
        val readArticles = ArrayList<Article>()
        readArticles.add(Article(3, "read", "http://www.google.com",
                Article.READ, "1", 1, 3, "feed", ""))
        readArticles.add(Article(4, "read1", "http://www.google.com",
                Article.READ, "1", 1, 4, "feed", ""))
        `when`(adapter.getAllArticlesInAFeed(testId, true)).thenReturn(readArticles)
        `when`(adapter.getAllArticlesOfCuration(testId, true)).thenReturn(readArticles)
        return adapter
    }

    private inner class MockView : ArticleListView {
        override val firstVisiblePosition: Int
            get() = 1
        override val lastVisiblePosition: Int
            get() = 1
        override val isBottomVisible: Boolean
            get() = false

        internal var isOpenedInternalWebView = false
        internal var isOpenedExternalWebView = false
        internal var isNoSearchResultShowed = false
        internal var isNoArticleShowed = false
        internal var shareUrl: String? = null
        internal var openedUrl: String? = null
        internal var openTitle: String? = null

        override fun openInternalWebView(url: String, rssTitle: String) {
            isOpenedInternalWebView = true
            openedUrl = url
            openTitle = rssTitle
        }

        override fun openExternalWebView(url: String) {
            isOpenedExternalWebView = true
            openedUrl = url
        }

        override fun notifyListView() {

        }

        override fun finish() {}

        override fun showShareUi(url: String) {
            shareUrl = url
        }

        override fun scrollTo(position: Int) {

        }

        override fun showEmptyView() {
            isNoArticleShowed = true
        }

        override fun showNoSearchResult() {
            isNoSearchResultShowed = true
        }
    }
}
