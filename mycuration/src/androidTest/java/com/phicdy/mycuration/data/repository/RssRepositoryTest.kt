package com.phicdy.mycuration.data.repository

import androidx.test.InstrumentationRegistry.getTargetContext
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.deleteAll
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class RssRepositoryTest {

    private lateinit var rssRepository: RssRepository
    private lateinit var articleRepository: ArticleRepository
    private lateinit var filterRepository: FilterRepository

    @Before
    fun setUp() {
        val db = DatabaseHelper(getTargetContext()).writableDatabase
        articleRepository = ArticleRepository(db)
        filterRepository = FilterRepository(db)
        rssRepository = RssRepository(db, articleRepository, filterRepository)
        deleteAll(db)
    }

    @After
    fun tearDown() {
        val db = DatabaseHelper(getTargetContext()).writableDatabase
        deleteAll(db)
    }

    @Test
    fun whenDeleteRSSThenTheRSSAndRelatedArticlesAndFiltersAreDeleted() = runBlocking {
        val rss = rssRepository.store("title", "http://www.google.com", "RSS", "http://yahoo.co.jp")
        rss?.let {
            val rssList = arrayListOf(rss)
            filterRepository.saveNewFilter("hoge", rssList, "keyword", "")

            // Store filter that relates two RSS, means not deleted by deleting one of the RSS
            val rss2 = rssRepository.store("testrss2", "http://www.hoge.com", "RSS", "http://www.hoge.com")
            rss2?.let {
                rssList += rss2
            } ?: fail("RSS2 is null")
            filterRepository.saveNewFilter(NOT_DELETED_FILTER_TITLE, rssList, NOT_DELETED_FILTER_KEYWORD, "")
        } ?: fail("RSS is null")


        // Delete rss
        val rssId = rss!!.id
        assertTrue(rssRepository.deleteRss(rssId))

        // Check the rss and related data was deleted
        assertNull(rssRepository.getFeedById(rssId))
        val articles = articleRepository.getAllArticlesInRss(rssId, true)
        assertNotNull(articles)
        assertThat(articles.size, `is`(0))
        var filters = filterRepository.getEnabledFiltersOfFeed(rssId)
        assertNotNull(filters)
        assertThat(filters.size, `is`(0))

        // Check filter that contains other rss was not deleted
        filters = filterRepository.getAllFilters()
        assertNotNull(filters)
        assertThat(filters.size, `is`(1))
        val (_, title, keyword) = filters[0]
        assertThat(title, `is`(NOT_DELETED_FILTER_TITLE))
        assertThat(keyword, `is`(NOT_DELETED_FILTER_KEYWORD))
    }

   companion object {
        private const val NOT_DELETED_FILTER_TITLE = "notDeletedFilterTitle"
        private const val NOT_DELETED_FILTER_KEYWORD = "notDeletedFilterKeyword"
    }

}