package com.phicdy.mycuration.articlelist

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
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
        presenter = ArticleListPresenter()
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
        assertThat(view.shareUrl).isEqualTo(longClickedArticle.url)
        return@runBlocking
    }

    @Test
    fun `No search result view shows when search action and no result`() = runBlocking {
        val article = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        mock1ArticleDatabase(article)
        val presenter = ArticleListPresenter()
        whenever(articleRepository.searchArticles("ghaeogha", true)).thenReturn(arrayListOf())
        val view = MockView()
        presenter.setView(view)
        presenter.createView()
        assertThat(view.isNoSearchResultShowed).isTrue()
        return@runBlocking
    }

    @Test
    fun `No article view shows when no search action and no result`() = runBlocking {
        mockEmptyDatabase(1)
        val view = MockView()
        presenter.setView(view)
        presenter.createView()
        assertThat(view.isNoArticleShowed).isTrue()
        return@runBlocking
    }

    @Test
    fun `Search article when search action`() = runBlocking {
        val article = Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "")
        mock1ArticleDatabase(article)
        val query = "query"
        whenever(articleRepository.searchArticles(query, true)).thenReturn(arrayListOf(article))

        val presenter = ArticleListPresenter()
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
