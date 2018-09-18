package com.phicdy.mycuration.domain.task

import android.support.test.InstrumentationRegistry.getTargetContext
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.rss.Article
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.ArrayList

class GetHatenaBookmarkTest {

    @Before
    fun setup() {
        DatabaseAdapter.setUp(DatabaseHelper(getTargetContext()))
        val adapter = DatabaseAdapter.getInstance()
        adapter.deleteAll()
    }

    @After
    fun tearDown() {
        val adapter = DatabaseAdapter.getInstance()
        adapter.deleteAll()
    }

    @Test
    fun myQiitaArticleReturnsZero() {
        val adapter = DatabaseAdapter.getInstance()
        val testFeed = adapter.saveNewFeed("test", "http://hoge.com", "hoge", "")
        val articles = ArrayList<Article>()
        val testUrl = "http://qiita.com/phicdy/items/1bcce3d6f040fc48f7bf"
        articles.add(Article(1, "hoge", testUrl, Article.UNREAD, "", 1, testFeed!!.id, "", ""))
        adapter.saveNewArticles(articles, testFeed.id)

        val getHatenaBookmark = GetHatenaBookmark(adapter)
        getHatenaBookmark.request(testUrl, 0)
        Thread.sleep(2000)

        assertThat(adapter.getAllUnreadArticles(true)[0].point, `is`("0"))
    }

    @Test
    fun myBlogArticleReturns1() {
        val adapter = DatabaseAdapter.getInstance()

        // Save test feed and article
        val testFeed = adapter.saveNewFeed("test", "http://hoge.com", "hoge", "")
        val articles = ArrayList<Article>()
        val testUrl = "http://phicdy.hatenablog.com/entry/2014/09/01/214055"
        articles.add(Article(1, "hoge", testUrl, Article.UNREAD, "", 1, testFeed!!.id, "", ""))
        adapter.saveNewArticles(articles, testFeed.id)

        // Start request
        val getHatenaBookmark = GetHatenaBookmark(adapter)
        getHatenaBookmark.request(testUrl, 0)
        Thread.sleep(3000)

        assertThat(adapter.getAllUnreadArticles(true)[0].point, `is`("1"))
    }
}