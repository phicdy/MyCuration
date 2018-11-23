package com.phicdy.mycuration.data.repository

import android.support.test.InstrumentationRegistry.getTargetContext
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.db.DatabaseHelper
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class UnreadCountRepositoryTest {

    private lateinit var rssRepository: RssRepository
    private lateinit var articleRepository: ArticleRepository
    private lateinit var filterRepository: FilterRepository
    private lateinit var curationRepository: CurationRepository
    private lateinit var unreadCountRepository: UnreadCountRepository
    private lateinit var adapter: DatabaseAdapter

    @Before
    fun setUp() {
        val db = DatabaseHelper(getTargetContext()).writableDatabase
        articleRepository = ArticleRepository(db)
        curationRepository = CurationRepository(db)
        filterRepository = FilterRepository(db)
        rssRepository = RssRepository(db, articleRepository, filterRepository)
        unreadCountRepository = UnreadCountRepository(rssRepository, curationRepository)
        adapter = DatabaseAdapter.getInstance()
        adapter.deleteAll()
    }

    @After
    fun tearDown() {
        adapter.deleteAll()
    }

    @Test
    fun whenAppendUnreadCountThenUnreadCountIncreases() = runBlocking {
        val rss = adapter.saveNewFeed("title", "http://www.google.com", "RSS", "http://yahoo.co.jp")
        rssRepository.updateUnreadArticleCount(rss.id, 1)
        unreadCountRepository.retrieve()
        unreadCountRepository.appendUnreadArticleCount(rss.id, 10)
        val updatedRss = rssRepository.getFeedWithUnreadCountBy(rss.id)
        assertNotNull(updatedRss)
        assertThat(updatedRss?.unreadAriticlesCount, `is`(11))
    }
}