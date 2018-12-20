package com.phicdy.mycuration.data.db

import android.support.test.InstrumentationRegistry.getTargetContext
import android.support.test.rule.ActivityTestRule
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.data.rss.Article
import com.phicdy.mycuration.deleteAll
import com.phicdy.mycuration.presentation.view.activity.TopActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.ArrayList
import java.util.Date

class DatabaseAdapterTest {

    private lateinit var adapter: DatabaseAdapter
    private lateinit var rssRepository: RssRepository
    private lateinit var articleRepository: ArticleRepository
    private lateinit var curationRepository: CurationRepository
    private val testUnreadArticles = ArrayList<Article>()
    private val testReadArticles = ArrayList<Article>()

    @JvmField
    @Rule
    var mActivityRule = ActivityTestRule(TopActivity::class.java)

    @Before
    fun setUp() {
        val helper = DatabaseHelper(getTargetContext())
        DatabaseAdapter.setUp(helper)
        adapter = DatabaseAdapter.getInstance()
        rssRepository = RssRepository(
                helper.writableDatabase,
                ArticleRepository(helper.writableDatabase),
                FilterRepository(helper.writableDatabase)
        )
        articleRepository = ArticleRepository(helper.writableDatabase)
        curationRepository = CurationRepository(helper.writableDatabase)
        deleteAll(helper.writableDatabase)
        insertTestData()
    }

    @After
    fun tearDown() {
        val db = DatabaseHelper(getTargetContext()).writableDatabase
        deleteAll(db)
    }

    @Test
    fun testSaveNewArticles() = runBlocking {
        // Reset data and insert curation at first
        insertTestCurationForArticle1()
        insertTestData()

        val savedArticles = articleRepository.getTop300Articles(false)
        if (savedArticles.size != 0) {
            val (_, title, _, status) = savedArticles[0]
            assertEquals(TEST_ARTICLE1_TITLE, title)
            assertEquals(Article.UNREAD, status)

            val (_, title1, _, status1) = savedArticles[1]
            assertEquals(TEST_ARTICLE2_TITLE, title1)
            assertEquals(Article.UNREAD, status1)

            val (_, title2, _, status2) = savedArticles[2]
            assertEquals(TEST_ARTICLE3_TITLE, title2)
            assertEquals(Article.UNREAD, status2)
        }

        val curatioId = adapter.getCurationIdByName(TEST_CURATION_NAME)
        val articles = adapter.getAllArticlesOfCuration(curatioId, true)
        assertNotNull(articles)
        assertEquals(1, articles.size)
        assertEquals(TEST_ARTICLE1_TITLE, articles[0].title)
    }

    private fun insertTestData() = runBlocking {
        rssRepository.store(TEST_FEED_TITLE, TEST_FEED_URL, "RSS", TEST_FEED_URL)
        val id = rssRepository.getFeedByUrl(TEST_FEED_URL)?.id ?: -1

        val now = Date().time
        val article = Article(1, TEST_ARTICLE1_TITLE, "http://www.google.com",
                Article.UNREAD, "", now, id, "", "")
        val quotationTitle = Article(1, TEST_ARTICLE2_TITLE,
                "http://www.google.com", Article.UNREAD, "", now + 1, id, "", "")
        val doubleQuotationTitle = Article(1, TEST_ARTICLE3_TITLE,
                "http://www.google.com", Article.UNREAD, "", now + 2, id, "", "")
        val japaneseTitle = Article(1, "記事1abdｄｆｇ",
                "http://www.google.com", Article.UNREAD, "", now + 2, id, "", "")

        testUnreadArticles.apply {
            clear()
            add(article)
            add(quotationTitle)
            add(doubleQuotationTitle)
            add(japaneseTitle)
        }
        val savedArticles = articleRepository.saveNewArticles(testUnreadArticles, id)
        curationRepository.saveCurationsOf(savedArticles)
        rssRepository.updateUnreadArticleCount(id, testUnreadArticles.size)

        val readArticle = Article(1, "readArticle", "http://www.google.com/read",
                Article.READ, "", now, id, "", "")
        testReadArticles.clear()
        testReadArticles.add(readArticle)
        val savedArtices = articleRepository.saveNewArticles(testReadArticles, id)
        curationRepository.saveCurationsOf(savedArtices)
    }

    @Test
    fun testSaveAllStatusToReadFromToRead() = runBlocking {
        val id = rssRepository.getFeedByUrl(TEST_FEED_URL)?.id ?: -1

        val articles = ArrayList<Article>()
        val now = System.currentTimeMillis()
        val toReadArticle = Article(1, "toread_article",
                "http://www.google.com", Article.TOREAD, "", now + 1, id, "", "")
        val toReadArticle2 = Article(1, "toread_article2",
                "http://www.google.com/hogehoge", Article.TOREAD, "", now + 2, id, "", "")
        articles.add(toReadArticle)
        articles.add(toReadArticle2)
        articleRepository.saveNewArticles(articles, id)

        val db = DatabaseHelper(getTargetContext()).writableDatabase
        val repository = ArticleRepository(db)
        repository.saveAllStatusToReadFromToRead()
        val changedArticles = articleRepository.getTop300Articles(true)
        var existToReadArticle = false
        for ((_, _, _, status) in changedArticles) {
            if (status == Article.TOREAD) {
                existToReadArticle = true
            }
        }
        assertEquals(false, existToReadArticle)
    }


    @Test
    fun testDeleteCuration() = runBlocking {
        insertTestCuration()
        val curationId = adapter.getCurationIdByName(TEST_CURATION_NAME)
        assertTrue(curationRepository.delete(curationId))
        assertEquals(DatabaseAdapter.NOT_FOUND_ID, adapter.getCurationIdByName(TEST_CURATION_NAME))
    }

    @Test
    fun testDeleteAllCuration() = runBlocking {
        insertTestCuration()
        assertTrue(adapter.deleteAllCuration())
        val map = curationRepository.getAllCurationWords()
        assertEquals(0, map.size)
    }

    @Test
    fun testGetAllArticlesOfCuration() = runBlocking {
        insertTestCurationForArticle1()
        val curationId = adapter.getCurationIdByName(TEST_CURATION_NAME)

        val wordsOfCurationForArticle1 = ArrayList<String>().apply {
            add(TEST_ARTICLE1_TITLE)
            add(TEST_WORD2)
            add(TEST_WORD3)
        }
        assertTrue(curationRepository.adaptToArticles(curationId, wordsOfCurationForArticle1))
        val articles = adapter.getAllArticlesOfCuration(curationId,  true)
        assertNotNull(articles)
        assertEquals(1, articles.size)
        assertEquals(TEST_ARTICLE1_TITLE, articles[0].title)
    }

    private fun insertTestCuration() = runBlocking {
        val words = ArrayList<String>().apply {
            add(TEST_WORD1)
            add(TEST_WORD2)
            add(TEST_WORD3)
        }
        assertTrue(curationRepository.store(TEST_CURATION_NAME, words) > 0)
    }

    private fun insertTestCurationForArticle1() = runBlocking {
        val words = ArrayList<String>().apply {
            add(TEST_ARTICLE1_TITLE)
            add(TEST_WORD2)
            add(TEST_WORD3)
        }
        assertTrue(curationRepository.store(TEST_CURATION_NAME, words) > 0)
    }

    companion object {

        private const val TEST_CURATION_NAME = "test"
        private const val TEST_WORD1 = "word1"
        private const val TEST_WORD2 = "word2"
        private const val TEST_WORD3 = "word3"
        private const val TEST_FEED_TITLE = "testfeed"
        private const val TEST_FEED_URL = "http://www.yahoo.co.jp"
        private const val TEST_ARTICLE1_TITLE = "title1"
        private const val TEST_ARTICLE2_TITLE = "title'"
        private const val TEST_ARTICLE3_TITLE = "title" + '"'
    }

}
