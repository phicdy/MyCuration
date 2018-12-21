package com.phicdy.mycuration.presentation.presenter

import android.content.Intent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.data.rss.Article
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.presentation.view.ArticleListView
import com.phicdy.mycuration.util.PreferenceHelper
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.ArrayList

class ArticleListPresenterTest {

    private val preferenceHelper = mock<PreferenceHelper>()
    private val unreadCountRepository = mock<UnreadCountRepository>()
    private val rssRepository = mock<RssRepository>()
    private val articleRepository = mock<ArticleRepository>()
    private lateinit var presenter: ArticleListPresenter
    private val testFeedId = 1

    @Before
    fun setup() {
        whenever(preferenceHelper.isOpenInternal).thenReturn(true)
        whenever(preferenceHelper.allReadBack).thenReturn(true)
        whenever(preferenceHelper.sortNewArticleTop).thenReturn(true)
        whenever(preferenceHelper.swipeDirection).thenReturn(PreferenceHelper.SWIPE_LEFT_TO_RIGHT)
        presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, rssRepository, preferenceHelper,
                articleRepository, unreadCountRepository,
                "", "")
    }

    @Test
    fun testOnCreate() {
        // For coverage
        val view = MockView()
        presenter.setView(view)
        presenter.create()
    }

    @Test
    fun `No articles are loaded after onCreateView with empty DB for feed`() = runBlocking {
        mockEmptyDatabase(testFeedId)
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertThat(presenter.articleSize(), `is`(0))
    }

    @Test
    fun `No Articles are loaded after onCreateView with empty DB for all feed`() = runBlocking {
        val testFeedId = Feed.ALL_FEED_ID
        mockEmptyDatabase(testFeedId)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, rssRepository, preferenceHelper, articleRepository, unreadCountRepository,
                "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertThat(presenter.articleSize(), `is`(0))
    }

    @Test
    fun `No Articles are loaded after onCreateView with empty DB for curation`() = runBlocking {
        val testCurationId = 1
        mockEmptyDatabase(testCurationId)
        val presenter = ArticleListPresenter(
                Feed.DEFAULT_FEED_ID, testCurationId, rssRepository, preferenceHelper, articleRepository, unreadCountRepository,
                "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertThat(presenter.articleSize(), `is`(0))
    }

    @Test
    fun `All artciles of feed are loaded after onCreateView if already read`() = runBlocking {
        mock1ReadArticleDatabase(testFeedId)
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertThat(presenter.articleSize(), `is`(1))
    }

    @Test
    fun `All artciles are loaded after onCreateView if already read`() = runBlocking {
        val testFeedId = Feed.ALL_FEED_ID
        mock1ReadArticleDatabase(testFeedId)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, rssRepository, preferenceHelper, articleRepository, unreadCountRepository,
                "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertThat(presenter.articleSize(), `is`(1))
    }

    @Test
    fun `All artciles OfCurationare loaded after onCreateView if already read`() = runBlocking {
        val testCurationId = 1
        mock1ReadArticleDatabase(testCurationId)
        val presenter = ArticleListPresenter(
                Feed.DEFAULT_FEED_ID, testCurationId, rssRepository, preferenceHelper, articleRepository, unreadCountRepository,
                "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertThat(presenter.articleSize(), `is`(1))
    }

    @Test
    fun `All unread articles of feed are only loaded after onCreateView`() = runBlocking {
        mock2Unread2ReadArticleDatabase(testFeedId)
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertTrue(presenter.isAllUnreadArticle)
    }

    @Test
    fun `All unread articles are only loaded after onCreateView`() = runBlocking {
        val testFeedId = Feed.ALL_FEED_ID
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, rssRepository, preferenceHelper, articleRepository, unreadCountRepository,
                "", "")
        mock2Unread2ReadArticleDatabase(testFeedId)
        val view = MockView()
        presenter.setView(view)
        presenter.createView()
        assertTrue(presenter.isAllUnreadArticle)
    }

    @Test
    fun `All unread articles of curation are only loaded after onCreateView`() = runBlocking {
        val testCurationId = 1
        mock2Unread2ReadArticleDatabase(testCurationId)
        val presenter = ArticleListPresenter(
                Feed.DEFAULT_FEED_ID, testCurationId, rssRepository, preferenceHelper, articleRepository, unreadCountRepository,
                "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        assertTrue(presenter.isAllUnreadArticle)
    }

    @Test
    fun testOnPause() {
        // For coverage
        val view = MockView()
        presenter.setView(view)
        presenter.pause()
    }

    @Test
    fun `unread article status becomes to read when clicked`() = runBlocking {
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        mock1ArticleDatabase(clickedArticle)
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertThat(clickedArticle.status, `is`(Article.TOREAD))
    }

    @Test
    fun `Toread article status is still to read when clicked`() = runBlocking {
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.TOREAD, "1", 1, 1, "feed", "")
        mock1ArticleDatabase(clickedArticle)
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertThat(clickedArticle.status, `is`(Article.TOREAD))
    }

    @Test
    fun `Read article status is still read when clicked`() = runBlocking {
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.READ, "1", 1, 1, "feed", "")
        mock1ArticleDatabase(clickedArticle)
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertThat(clickedArticle.status, `is`(Article.READ))
    }

    @Test
    fun `Article is opened with internal option when clicked`() = runBlocking {
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        mock1ArticleDatabase(clickedArticle)
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertThat<String>(view.openedUrl, `is`(clickedArticle.url))
    }

    @Test
    fun `Article is opened with external option when clicked`() = runBlocking {
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        mock1ArticleDatabase(clickedArticle)
        whenever(preferenceHelper.isOpenInternal).thenReturn(false)
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertThat<String>(view.openedUrl, `is`(clickedArticle.url))
    }

    @Test
    fun `Intenal web view is opened with internal option when clicked`() = runBlocking {
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        mock1ArticleDatabase(clickedArticle)
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertTrue(view.isOpenedInternalWebView)
    }

    @Test
    fun `Title of intenal web view is opended article's RSS's title`() = runBlocking {
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        mock1ArticleDatabase(clickedArticle)
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertThat(view.openTitle, `is`(clickedArticle.feedTitle))
    }

    @Test
    fun `Title of intenal web view is opened article's RSS's title for all RSS`() = runBlocking {
        val testFeedId = Feed.ALL_FEED_ID
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        mock1ArticleDatabase(clickedArticle)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, rssRepository, preferenceHelper, articleRepository, unreadCountRepository,
                "", "")
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertThat(view.openTitle, `is`(clickedArticle.feedTitle))
    }

    @Test
    fun `Extenal web view is opened with external option when clicked`() = runBlocking {
        val clickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        mock1ArticleDatabase(clickedArticle)
        whenever(preferenceHelper.isOpenInternal).thenReturn(false)
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemClicked(0)
        assertTrue(view.isOpenedExternalWebView)
    }

    @Test
    fun `Share UI shows when long clicked`() = runBlocking {
        val longClickedArticle = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        mock1ArticleDatabase(longClickedArticle)
        val view = MockView()
        presenter.setView(view)
        presenter.create()
        presenter.createView()
        presenter.onListItemLongClicked(0)
        assertThat<String>(view.shareUrl, `is`(longClickedArticle.url))
    }

    @Test
    fun `No search result view shows when search action and no result`() = runBlocking {
        val article = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        mock1ArticleDatabase(article)
        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, rssRepository, preferenceHelper, articleRepository,
                unreadCountRepository, "ghaeogha",
                Intent.ACTION_SEARCH)
        whenever(articleRepository.searchArticles( "ghaeogha", true)).thenReturn(arrayListOf())
        val view = MockView()
        presenter.setView(view)
        presenter.createView()
        assertTrue(view.isNoSearchResultShowed)
    }

    @Test
    fun `No article view shows when no search action and no result`() = runBlocking {
        mockEmptyDatabase(1)
        val view = MockView()
        presenter.setView(view)
        presenter.createView()
        assertTrue(view.isNoArticleShowed)
    }

    @Test
    fun `Search article when search action`() = runBlocking {
        val article = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        mock1ArticleDatabase(article)
        val query = "query"
        whenever(articleRepository.searchArticles(query, true)).thenReturn(arrayListOf(article))

        val presenter = ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, rssRepository, preferenceHelper, articleRepository, unreadCountRepository,
                query, Intent.ACTION_SEARCH)
        val view = MockView()
        presenter.setView(view)
        presenter.createView()
        verify(articleRepository, times(1)).searchArticles(query, true)
        return@runBlocking
    }

    private fun mockEmptyDatabase(testId: Int) = runBlocking {
        whenever(articleRepository.getUnreadArticlesOfRss(testId, true)).thenReturn(ArrayList())
        whenever(articleRepository.getAllUnreadArticles(true)).thenReturn(arrayListOf())
        whenever(articleRepository.isExistArticleOf(testId)).thenReturn(false)
        whenever(articleRepository.isExistArticle()).thenReturn(false)
        whenever(articleRepository.getAllUnreadArticlesOfCuration(testId, true)).thenReturn(ArrayList())
        whenever(articleRepository.getAllArticlesOfCuration(testId, true)).thenReturn(ArrayList())
    }

    private fun mock1ReadArticleDatabase(testId: Int) = runBlocking {
        whenever(articleRepository.getUnreadArticlesOfRss(testId, true)).thenReturn(ArrayList())
        whenever(articleRepository.getAllUnreadArticlesOfCuration(testId, true)).thenReturn(ArrayList())
        whenever(articleRepository.getAllUnreadArticles(true)).thenReturn(arrayListOf())
        whenever(articleRepository.isExistArticleOf(testId)).thenReturn(true)
        whenever(articleRepository.isExistArticle()).thenReturn(true)
        val articles = ArrayList<Article>()
        articles.add(Article(1, "hoge", "http://www.google.com",
                Article.READ, "1", 1, 1, "feed", ""))
        whenever(articleRepository.getAllArticlesOfRss(testId, true)).thenReturn(articles)
        whenever(articleRepository.getTop300Articles(true)).thenReturn(articles)
        whenever(articleRepository.getAllArticlesOfCuration(testId, true)).thenReturn(articles)
    }

    private fun mock1ArticleDatabase(article: Article) = runBlocking {
        val testId = 1
        val articles = ArrayList<Article>()
        articles.add(article)
        whenever(articleRepository.getAllUnreadArticles(true)).thenReturn(articles)
        whenever(articleRepository.getTop300Articles(true)).thenReturn(articles)
        whenever(articleRepository.getUnreadArticlesOfRss(testId, true)).thenReturn(articles)
        whenever(articleRepository.isExistArticleOf(testId)).thenReturn(true)
        val mockFeed = mock<Feed>()
        whenever(mockFeed.title).thenReturn("feed")
        whenever(rssRepository.getFeedById(testId)).thenReturn(mockFeed)
    }

    private fun mock2Unread2ReadArticleDatabase(testId: Int) = runBlocking {
        val unreadArticles = ArrayList<Article>()
        unreadArticles.add(Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", ""))
        unreadArticles.add(Article(2, "unread2", "http://www.google.com",
                Article.UNREAD, "1", 1, 2, "feed", ""))
        whenever(articleRepository.getUnreadArticlesOfRss(testId, true)).thenReturn(unreadArticles)
        whenever(articleRepository.getAllUnreadArticlesOfCuration(testId, true)).thenReturn(unreadArticles)
        whenever(articleRepository.getAllUnreadArticles(true)).thenReturn(unreadArticles)
        whenever(articleRepository.isExistArticleOf(testId)).thenReturn(true)
        whenever(articleRepository.isExistArticle()).thenReturn(true)
        val readArticles = ArrayList<Article>()
        readArticles.add(Article(3, "read", "http://www.google.com",
                Article.READ, "1", 1, 3, "feed", ""))
        readArticles.add(Article(4, "read1", "http://www.google.com",
                Article.READ, "1", 1, 4, "feed", ""))
        whenever(articleRepository.getAllArticlesOfRss(testId, true)).thenReturn(readArticles)
        whenever(articleRepository.getAllArticlesOfCuration(testId, true)).thenReturn(readArticles)
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
