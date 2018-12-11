package com.phicdy.mycuration.data.repository

import android.support.test.InstrumentationRegistry.getTargetContext
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.rss.Article
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith
import java.util.Date

@RunWith(Theories::class)
class ArticleRepositoryTest {

    private lateinit var articleRepository: ArticleRepository
    private lateinit var rssRepository: RssRepository
    private lateinit var adapter: DatabaseAdapter

    @Before
    fun setUp() {
        DatabaseAdapter.setUp(DatabaseHelper(getTargetContext()))
        val db = DatabaseHelper(getTargetContext()).writableDatabase
        articleRepository = ArticleRepository(db)
        rssRepository = RssRepository(
                db,
                articleRepository,
                FilterRepository(db)
        )
        adapter = DatabaseAdapter.getInstance()
        adapter.deleteAll()
    }

    @After
    fun tearDown() {
        adapter.deleteAll()
    }

    @Theory
    fun whenSearchJapaneseArticle_ThenReturnTheArticle(title: String) = runBlocking {
        val rss = rssRepository.store(TEST_FEED_TITLE, TEST_FEED_URL, "RSS", TEST_FEED_URL)
        rss?.let {
            val testUnreadArticles = arrayListOf(Article(1, title,
                    TEST_ARTICLE_URL, Article.UNREAD, "", Date().time, rss.id, "", "")
            )
            articleRepository.saveNewArticles(testUnreadArticles, rss.id)
            val list = articleRepository.searchArticles(title, true)
            assertEquals(1, list.size)
            assertEquals(title, list[0].title)
        } ?: fail("RSS is null")
    }

    @Test
    fun aaa() {
        assertEquals(1, 1)
    }

    companion object {

        private const val TEST_ARTICLE_URL = "http://www.google.com/article"
        private const val TEST_FEED_TITLE = "testfeed"
        private const val TEST_FEED_URL = "http://www.yahoo.co.jp"

        @DataPoints
        @JvmField
        val titles = listOf("記事1abdｄｆｇ", "aaa%bbbb", "cccddd_")
    }

}