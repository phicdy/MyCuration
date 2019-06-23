package com.phicdy.mycuration.data.repository

import androidx.test.core.app.ApplicationProvider
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.deleteAll
import com.phicdy.mycuration.domain.entity.Feed
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class FilterRepositoryTest {

    private lateinit var rssRepository: RssRepository
    private lateinit var articleRepository: ArticleRepository
    private lateinit var filterRepository: FilterRepository

    @Before
    fun setUp() {
        val db = DatabaseHelper(ApplicationProvider.getApplicationContext()).writableDatabase
        articleRepository = ArticleRepository(db)
        filterRepository = FilterRepository(db)
        rssRepository = RssRepository(db, articleRepository, filterRepository)
        deleteAll(db)
    }

    @After
    fun tearDown() {
        val db = DatabaseHelper(ApplicationProvider.getApplicationContext()).writableDatabase
        deleteAll(db)
    }

    @Test
    fun whenGetEnabledFilter_ThenReturnTheFilter() = runBlocking {
        val stored = rssRepository.store("title", "http://www.google.com", Feed.ATOM, "http://www.google.com")
        val filterTitle = "filter"
        val keyword = "keyword"
        val filterUrl = "http://filter.com"
        stored?.let {
            filterRepository.saveNewFilter(filterTitle, arrayListOf(stored), keyword, filterUrl)
            val filters = filterRepository.getEnabledFiltersOfFeed(stored.id)
            assertThat(filters.size, `is`(1))
            assertThat(filters[0].title, `is`(filterTitle))
            assertThat(filters[0].keyword, `is`(keyword))
            assertThat(filters[0].url, `is`(filterUrl))
        } ?: fail("Failed to store RSS")
    }

    @Test
    fun whenStoreDisabledFilterAndGetEnabledFilter_ThenReturnEmpty() = runBlocking {
        val stored = rssRepository.store("title", "http://www.google.com", Feed.ATOM, "http://www.google.com")
        val filterTitle = "filter"
        val keyword = "keyword"
        val filterUrl = "http://filter.com"

        stored?.let {
            filterRepository.saveNewFilter(filterTitle, arrayListOf(stored), keyword, filterUrl)

            // Disable the filter
            val filter = filterRepository.getAllFilters()[0]
            filterRepository.updateEnabled(filter.id, false)
            val filters = filterRepository.getEnabledFiltersOfFeed(stored.id)
            assertThat(filters.size, `is`(0))
        } ?: fail("Failed to store RSS")
    }
}