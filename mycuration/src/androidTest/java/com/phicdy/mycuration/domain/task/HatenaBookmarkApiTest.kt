package com.phicdy.mycuration.domain.task

import android.support.test.InstrumentationRegistry.getTargetContext
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.network.HatenaBookmarkApi
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.rss.Article
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.ArrayList

class HatenaBookmarkApiTest {

    private lateinit var articleRepository: ArticleRepository

    @Before
    fun setup() {
        val helper = DatabaseHelper(getTargetContext())
        DatabaseAdapter.setUp(helper)
        val adapter = DatabaseAdapter.getInstance()
        articleRepository = ArticleRepository(helper.writableDatabase)
        adapter.deleteAll()
    }

    @After
    fun tearDown() {
        val adapter = DatabaseAdapter.getInstance()
        adapter.deleteAll()
    }

    @Test
    fun myQiitaArticleReturnsZero() = runBlocking {
        val adapter = DatabaseAdapter.getInstance()
        val testFeed = adapter.saveNewFeed("test", "http://hoge.com", "hoge", "")
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
        val adapter = DatabaseAdapter.getInstance()

        // Save test feed and article
        val testFeed = adapter.saveNewFeed("test", "http://hoge.com", "hoge", "")
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