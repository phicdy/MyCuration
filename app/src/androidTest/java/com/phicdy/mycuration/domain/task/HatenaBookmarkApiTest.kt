package com.phicdy.mycuration.domain.task

import androidx.test.core.app.ApplicationProvider
import com.phicdy.mycuration.TestCoroutineDispatcherProvider
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.db.DatabaseMigration
import com.phicdy.mycuration.data.db.ResetIconPathTask
import com.phicdy.mycuration.data.network.HatenaBookmarkApi
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.deleteAll
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.ArrayList

@ExperimentalCoroutinesApi
class HatenaBookmarkApiTest {

    private val testCoroutineScope = TestCoroutineScope()
    private val testDispatcher = TestCoroutineDispatcher()
    private val testDispatcherProvider = TestCoroutineDispatcherProvider(testDispatcher)

    private lateinit var articleRepository: ArticleRepository
    private lateinit var rssRepository: RssRepository

    private val db = DatabaseHelper(ApplicationProvider.getApplicationContext(), DatabaseMigration(ResetIconPathTask())).writableDatabase

    @Before
    fun setup() {
        articleRepository = ArticleRepository(db, testDispatcherProvider)
        rssRepository = RssRepository(db, articleRepository, FilterRepository(db, testDispatcherProvider), testCoroutineScope, testDispatcherProvider)
        deleteAll(db)
    }

    @After
    fun tearDown() {
        deleteAll(db)
        testCoroutineScope.cleanupTestCoroutines()
    }

    @Test
    fun myQiitaArticleReturnsZero() = runBlocking {
        val testFeed = rssRepository.store("test", "http://hoge.com", "hoge", "")
        val articles = ArrayList<Article>()
        val testUrl = "http://qiita.com/phicdy/items/1bcce3d6f040fc48f7bf"
        articles.add(Article(1, "hoge", testUrl, Article.UNREAD, "", 1, testFeed!!.id, "", ""))
        articleRepository.saveNewArticles(articles, testFeed.id)

        val hatenaBookmarkApi = HatenaBookmarkApi()
        val point = hatenaBookmarkApi.request(testUrl)
        assertThat(point, `is`("0"))
    }

    @Test
    fun myBlogArticleReturns1() = runBlocking {
        // Save test feed and article
        val testFeed = rssRepository.store("test", "http://hoge.com", "hoge", "")
        val articles = ArrayList<Article>()
        val testUrl = "http://phicdy.hatenablog.com/entry/2014/09/01/214055"
        articles.add(Article(1, "hoge", testUrl, Article.UNREAD, "", 1, testFeed!!.id, "", ""))
        articleRepository.saveNewArticles(articles, testFeed.id)

        // Start request
        val hatenaBookmarkApi = HatenaBookmarkApi()
        val point = hatenaBookmarkApi.request(testUrl)
        assertThat(point, `is`("1"))
    }
}