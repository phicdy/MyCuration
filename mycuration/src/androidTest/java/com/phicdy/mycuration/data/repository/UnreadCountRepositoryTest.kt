package com.phicdy.mycuration.data.repository

import androidx.test.InstrumentationRegistry.getTargetContext
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.deleteAll
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
    private var rss: Feed? = null

    @Before
    fun setUp() = runBlocking {
        val db = DatabaseHelper(getTargetContext()).writableDatabase
        articleRepository = ArticleRepository(db)
        curationRepository = CurationRepository(db)
        filterRepository = FilterRepository(db)
        rssRepository = RssRepository(db, articleRepository, filterRepository)
        unreadCountRepository = UnreadCountRepository(rssRepository, curationRepository)
        deleteAll(db)

        rss = rssRepository.store("title", "http://www.google.com", "RSS", "http://yahoo.co.jp")
        assertNotNull(rss)
        rssRepository.updateUnreadArticleCount(rss!!.id, TEST_RSS_DEFAULT_UNREAD_COUNT)
        unreadCountRepository.retrieve()
    }

    @After
    fun tearDown() {
        val db = DatabaseHelper(getTargetContext()).writableDatabase
        deleteAll(db)
    }

    @Test
    fun whenAppendUnreadCount_ThenUnreadCountIncreases() = runBlocking {
        unreadCountRepository.appendUnreadArticleCount(rss!!.id, 10)
        val updatedRss = rssRepository.getFeedWithUnreadCountBy(rss!!.id)
        assertNotNull(updatedRss)
        assertThat(updatedRss?.unreadAriticlesCount, `is`(TEST_RSS_DEFAULT_UNREAD_COUNT + 10))
    }

    @Test
    fun whenDecreaseCount_ThenUnreadCountDecreases() = runBlocking {
        unreadCountRepository.decreaseCount(rss!!.id, 4)
        val updatedRss = rssRepository.getFeedWithUnreadCountBy(rss!!.id)
        assertNotNull(updatedRss)
        assertThat(updatedRss?.unreadAriticlesCount, `is`(1))
        assertThat(unreadCountRepository.total, `is`(TEST_RSS_DEFAULT_UNREAD_COUNT - 4))
    }

    @Test
    fun whenTooBigDecreaseCount_ThenUnreadCountWillBe0() = runBlocking {
        unreadCountRepository.decreaseCount(rss!!.id, TEST_RSS_DEFAULT_UNREAD_COUNT + 1)
        val updatedRss = rssRepository.getFeedWithUnreadCountBy(rss!!.id)
        assertNotNull(updatedRss)
        assertThat(updatedRss?.unreadAriticlesCount, `is`(0))
        assertThat(unreadCountRepository.total, `is`(0))
    }

    companion object {
        private const val TEST_RSS_DEFAULT_UNREAD_COUNT = 5
    }
}