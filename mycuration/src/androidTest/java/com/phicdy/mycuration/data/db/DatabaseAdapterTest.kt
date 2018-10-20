package com.phicdy.mycuration.data.db

import android.support.test.InstrumentationRegistry.getTargetContext
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.rss.Article
import com.phicdy.mycuration.presentation.view.activity.TopActivity
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.ArrayList
import java.util.Date

@RunWith(AndroidJUnit4::class)
class DatabaseAdapterTest {

    private lateinit var adapter: DatabaseAdapter
    private val testUnreadArticles = ArrayList<Article>()
    private val testReadArticles = ArrayList<Article>()

    @JvmField
    @Rule
    var mActivityRule = ActivityTestRule(TopActivity::class.java)

    @Before
    fun setUp() {
        DatabaseAdapter.setUp(DatabaseHelper(getTargetContext()))
        adapter = DatabaseAdapter.getInstance()
        insertTestData()
    }

    @After
    fun tearDown() {
        adapter.deleteAll()
    }

    @Test
    fun testIsArticle() {
        for (testArticle in testUnreadArticles) {
            assertEquals(true, adapter.isArticle(testArticle))
        }
    }

    @Test
    fun testSaveNewArticles() {
        // Reset data and insert curation at first
        adapter.deleteAll()
        insertTestCurationForArticle1()
        insertTestData()

        val savedArticles = adapter.getTop300Articles(false)
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

    @Test
    fun testSearchArticles() {
        val list = adapter.searchArticles("記事1abdｄｆｇ", true)
        assertEquals(1, list.size)
        assertEquals("記事1abdｄｆｇ", list[0].title)
    }

    private fun insertTestData() {
        adapter.saveNewFeed(TEST_FEED_TITLE, TEST_FEED_URL, "RSS", TEST_FEED_URL)
        val id = adapter.getFeedByUrl(TEST_FEED_URL).id

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
        adapter.saveNewArticles(testUnreadArticles, id)
        adapter.updateUnreadArticleCount(id, testUnreadArticles.size)

        val readArticle = Article(1, "readArticle", "http://www.google.com/read",
                Article.READ, "", now, id, "", "")
        testReadArticles.clear()
        testReadArticles.add(readArticle)
        adapter.saveNewArticles(testReadArticles, id)
    }

    @Test
    fun testSaveAllStatusToReadFromToRead() = runBlocking {
        val id = adapter.getFeedByUrl(TEST_FEED_URL).id

        val articles = ArrayList<Article>()
        val now = System.currentTimeMillis()
        val toReadArticle = Article(1, "toread_article",
                "http://www.google.com", Article.TOREAD, "", now + 1, id, "", "")
        val toReadArticle2 = Article(1, "toread_article2",
                "http://www.google.com/hogehoge", Article.TOREAD, "", now + 2, id, "", "")
        articles.add(toReadArticle)
        articles.add(toReadArticle2)
        adapter.saveNewArticles(articles, id)

        val db = DatabaseHelper(getTargetContext()).writableDatabase
        val repository = ArticleRepository(db)
        repository.saveAllStatusToReadFromToRead()
        val changedArticles = adapter.getTop300Articles(true)
        var existToReadArticle = false
        for ((_, _, _, status) in changedArticles) {
            if (status == Article.TOREAD) {
                existToReadArticle = true
            }
        }
        assertEquals(false, existToReadArticle)
    }

    @Test
    fun testGetAllFeedsWithNumOfUnreadArticles() {
        val feeds = adapter.allFeedsWithNumOfUnreadArticles
        assertNotNull(feeds)
        assertEquals(1, feeds.size)
        val (_, title, url, _, _, unreadAriticlesCount) = feeds[0]
        assertEquals(TEST_FEED_TITLE, title)
        assertEquals(TEST_FEED_URL, url)
        assertEquals(testUnreadArticles.size, unreadAriticlesCount)
    }

    @Test
    fun testSaveNewCuration() {
        insertTestCuration()

        val curationId = adapter.getCurationIdByName(TEST_CURATION_NAME)
        val map = adapter.allCurationWords
        assertThat(map.indexOfKey(curationId), `is`(0))
        assertThat(map.size(), `is`(1))
        val addedWords = map.get(curationId)
        val TEST_WORDS_SIZE = 3
        assertEquals(TEST_WORDS_SIZE, addedWords.size)
        assertEquals(TEST_WORD1, addedWords[0])
        assertEquals(TEST_WORD2, addedWords[1])
        assertEquals(TEST_WORD3, addedWords[2])
    }

    @Test
    fun testGetAllCurationWords() {
        // No data
        var map = adapter.allCurationWords
        assertEquals(0, map.size())

        // 1 curation
        insertTestCuration()
        val curationId1 = adapter.getCurationIdByName(TEST_CURATION_NAME)

        map = adapter.allCurationWords
        assertEquals(1, map.size())
        assertThat(map.indexOfKey(curationId1), `is`(0))
        var addedWords1 = map.get(curationId1)
        assertEquals(TEST_WORD1, addedWords1[0])
        assertEquals(TEST_WORD2, addedWords1[1])
        assertEquals(TEST_WORD3, addedWords1[2])

        // 2 curations
        val curationName2 = "test2"
        val testWord4 = "word4"
        val testWord5 = "word5"
        val testWord6 = "word6"
        val words2 = ArrayList<String>().apply {
            add(testWord4)
            add(testWord5)
            add(testWord6)
        }
        assertTrue(adapter.saveNewCuration(curationName2, words2))
        val curationId2 = adapter.getCurationIdByName(curationName2)

        map = adapter.allCurationWords
        assertEquals(2, map.size())

        assertThat(map.indexOfKey(curationId1), `is`(0))
        addedWords1 = map.get(curationId1)
        assertEquals(TEST_WORD1, addedWords1[0])
        assertEquals(TEST_WORD2, addedWords1[1])
        assertEquals(TEST_WORD3, addedWords1[2])

        assertThat(map.indexOfKey(curationId2), `is`(1))
        val addedWords2 = map.get(curationId2)
        assertEquals(testWord4, addedWords2[0])
        assertEquals(testWord5, addedWords2[1])
        assertEquals(testWord6, addedWords2[2])
    }

    @Test
    fun testDeleteCuration() {
        insertTestCuration()
        val curationId = adapter.getCurationIdByName(TEST_CURATION_NAME)
        assertTrue(adapter.deleteCuration(curationId))
        assertEquals(DatabaseAdapter.NOT_FOUND_ID, adapter.getCurationIdByName(TEST_CURATION_NAME))
    }

    @Test
    fun testDeleteAllCuration() {
        insertTestCuration()
        assertTrue(adapter.deleteAllCuration())
        val map = adapter.allCurationWords
        assertEquals(0, map.size())
    }

    @Test
    fun testGetAllArticlesOfCuration() {
        insertTestCurationForArticle1()
        val curationId = adapter.getCurationIdByName(TEST_CURATION_NAME)

        val wordsOfCurationForArticle1 = ArrayList<String>().apply {
            add(TEST_ARTICLE1_TITLE)
            add(TEST_WORD2)
            add(TEST_WORD3)
        }
        assertTrue(adapter.adaptCurationToArticles(TEST_CURATION_NAME, wordsOfCurationForArticle1))
        val articles = adapter.getAllArticlesOfCuration(curationId, true)
        assertNotNull(articles)
        assertEquals(1, articles.size)
        assertEquals(TEST_ARTICLE1_TITLE, articles[0].title)
    }

    private fun insertTestCuration() {
        val words = ArrayList<String>().apply {
            add(TEST_WORD1)
            add(TEST_WORD2)
            add(TEST_WORD3)
        }
        assertTrue(adapter.saveNewCuration(TEST_CURATION_NAME, words))
    }

    private fun insertTestCurationForArticle1() {
        val words = ArrayList<String>().apply {
            add(TEST_ARTICLE1_TITLE)
            add(TEST_WORD2)
            add(TEST_WORD3)
        }
        assertTrue(adapter.saveNewCuration(TEST_CURATION_NAME, words))
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
