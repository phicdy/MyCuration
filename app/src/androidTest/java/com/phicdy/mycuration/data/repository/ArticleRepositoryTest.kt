package com.phicdy.mycuration.data.repository

import androidx.test.core.app.ApplicationProvider
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.deleteAll
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
    private lateinit var curationRepository: CurationRepository
    private val testUnreadArticles = ArrayList<Article>()
    private val testReadArticles = ArrayList<Article>()

    @Before
    fun setUp() {
        val db = DatabaseHelper(ApplicationProvider.getApplicationContext()).writableDatabase
        articleRepository = ArticleRepository(db)
        rssRepository = RssRepository(
                db,
                articleRepository,
                FilterRepository(db)
        )
        curationRepository = CurationRepository(db)
        deleteAll(db)
    }

    @After
    fun tearDown() {
        val db = DatabaseHelper(ApplicationProvider.getApplicationContext()).writableDatabase
        deleteAll(db)
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
    fun testSaveNewArticles() = runBlocking {
        // Reset data and insert curation at first
        val curationId = insertTestCurationForArticle1()
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

        val articles = articleRepository.getAllArticlesOfCuration(curationId, true)
        assertNotNull(articles)
        assertEquals(1, articles.size)
        assertEquals(TEST_ARTICLE1_TITLE, articles[0].title)
    }

    @Test
    fun testGetAllArticlesOfCuration() = testCoroutineScope.runBlockingTest {
        insertTestData()
        val curationId = insertTestCurationForArticle1()

        val wordsOfCurationForArticle1 = ArrayList<String>().apply {
            add(TEST_ARTICLE1_TITLE)
            add(TEST_WORD2)
            add(TEST_WORD3)
        }
        assertTrue(curationRepository.adaptToArticles(curationId, wordsOfCurationForArticle1))
        val articles = articleRepository.getAllArticlesOfCuration(curationId, true)
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

    private fun insertTestCurationForArticle1(): Int = runBlocking {
        val words = ArrayList<String>().apply {
            add(TEST_ARTICLE1_TITLE)
            add(TEST_WORD2)
            add(TEST_WORD3)
        }
        val id = curationRepository.store(TEST_CURATION_NAME, words).toInt()
        assertTrue(id > 0)
        return@runBlocking id
    }


    companion object {

        private const val TEST_ARTICLE_URL = "http://www.google.com/article"
        private const val TEST_ARTICLE1_TITLE = "title1"
        private const val TEST_ARTICLE2_TITLE = "title'"
        private const val TEST_ARTICLE3_TITLE = "title" + '"'
        private const val TEST_FEED_TITLE = "testfeed"
        private const val TEST_FEED_URL = "http://www.yahoo.co.jp"

        private const val TEST_CURATION_NAME = "test"
        private const val TEST_WORD1 = "word1"
        private const val TEST_WORD2 = "word2"
        private const val TEST_WORD3 = "word3"

        @DataPoints
        @JvmField
        val titles = listOf("記事1abdｄｆｇ", "aaa%bbbb", "cccddd_")
    }

}