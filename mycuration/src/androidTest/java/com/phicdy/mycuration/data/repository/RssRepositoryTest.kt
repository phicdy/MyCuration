package com.phicdy.mycuration.data.repository

import android.support.test.InstrumentationRegistry.getTargetContext
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.db.DatabaseHelper
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RssRepositoryTest {

    private lateinit var rssRepository: RssRepository
    private lateinit var articleRepository: ArticleRepository
    private lateinit var filterRepository: FilterRepository
    private lateinit var adapter: DatabaseAdapter

    @Before
    fun setUp() {
        DatabaseAdapter.setUp(DatabaseHelper(getTargetContext()))
        val db = DatabaseHelper(getTargetContext()).writableDatabase
        articleRepository = ArticleRepository(db)
        filterRepository = FilterRepository(db)
        rssRepository = RssRepository(db, articleRepository, filterRepository)
        adapter = DatabaseAdapter.getInstance()
        adapter.deleteAll()
    }

    @After
    fun tearDown() {
        adapter.deleteAll()
    }

    @Test
    fun whenDeleteRSSThenTheRSSAndRelatedArticlesAndFiltersAreDeleted() = runBlocking {
        val rss = adapter.saveNewFeed("title", "http://www.google.com", "RSS", "http://yahoo.co.jp")
        val rssList = arrayListOf(rss)
        adapter.saveNewFilter("hoge", rssList, "keyword", "")

        // Store filter that relates two RSS, means not deleted by deleting one of the RSS
        val rss2 = adapter.saveNewFeed("testrss2", "http://www.hoge.com", "RSS", "http://www.hoge.com")
        rssList += rss2
        adapter.saveNewFilter(NOT_DELETED_FILTER_TITLE, rssList, NOT_DELETED_FILTER_KEYWORD, "")

        // Delete rss
        val rssId = rss.id
        assertTrue(rssRepository.deleteRss(rssId))

        // Check the rss and related data was deleted
        assertNull(adapter.getFeedById(rssId))
        val articles = articleRepository.getAllArticlesInRss(rssId, true)
        assertNotNull(articles)
        assertThat(articles.size, `is`(0))
        var filters = adapter.getEnabledFiltersOfFeed(rssId)
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