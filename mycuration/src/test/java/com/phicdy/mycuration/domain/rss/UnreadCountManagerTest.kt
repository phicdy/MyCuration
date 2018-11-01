package com.phicdy.mycuration.domain.rss


import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.rss.Feed
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.ArrayList

class UnreadCountManagerTest {

    private lateinit var mockAdapter: DatabaseAdapter
    private val feed = Feed()

    companion object {
        private const val testUnreadCount = 5
    }

    @Before
    fun setUp() {
        mockAdapter = Mockito.mock(DatabaseAdapter::class.java)
        DatabaseAdapter.inject(mockAdapter)
        UnreadCountManager.inject(mockAdapter)
        val allFeedList = ArrayList<Feed>()
        Mockito.`when`(mockAdapter.allFeedsWithNumOfUnreadArticles).thenReturn(allFeedList)
        feed.unreadAriticlesCount = testUnreadCount
        UnreadCountManager.addFeed(feed)
    }

    @After
    fun tearDown() {
        UnreadCountManager.clear()
    }

    @Test
    fun totalIncreasesWhenRssIsAdded() {
        val currentTotal = UnreadCountManager.total
        assertThat(currentTotal, `is`(testUnreadCount))
    }

    @Test
    fun totalDoesNotChangeWhenNullIsAdded() {
        val total = UnreadCountManager.total
        UnreadCountManager.addFeed(null)
        val currentTotal = UnreadCountManager.total
        assertThat(currentTotal, `is`(total))
    }

    @Test
    fun totalDecreasesWhenRssIsDeleted() {
        UnreadCountManager.deleteFeed(feed.id)
        assertThat(UnreadCountManager.total, `is`(0))
    }

    @Test
    fun totalDoesNotChangeWhenInvalidRssIsDeleted() {
        UnreadCountManager.deleteFeed(-9999)
        val currentTotal = UnreadCountManager.total
        assertThat(currentTotal, `is`(testUnreadCount))
    }

    @Test
    fun totalIncreasesWhenCountUp() {
        UnreadCountManager.conutUpUnreadCount(feed.id)
        val currentTotal = UnreadCountManager.total
        assertThat(currentTotal, `is`(testUnreadCount + 1))
    }

    @Test
    fun totalDoesNotChangeWhenInvalidCountUp() {
        val total = UnreadCountManager.total
        UnreadCountManager.conutUpUnreadCount(-9999)
        val currentTotal = UnreadCountManager.total
        assertThat(currentTotal, `is`(total))
    }

    @Test
    fun totalDecreasesWhenCountDown() {
        UnreadCountManager.countDownUnreadCount(feed.id)
        val currentTotal = UnreadCountManager.total
        assertThat(currentTotal, `is`(testUnreadCount - 1))
    }

    @Test
    fun totalDoesNotChangeWhenInvalidCountDown() {
        val total = UnreadCountManager.total
        UnreadCountManager.countDownUnreadCount(-9999)
        val currentTotal = UnreadCountManager.total
        assertThat(currentTotal, `is`(total))
    }

    @Test
    fun totalKeeps0WhenCountDown() {
        for (i in 0 until feed.unreadAriticlesCount) {
            UnreadCountManager.countDownUnreadCount(feed.id)
        }
        UnreadCountManager.countDownUnreadCount(feed.id)
        val currentTotal = UnreadCountManager.total
        assertThat(currentTotal, `is`(0))
    }

    @Test
    fun unreadCountIsSameWithOneOfRss() {
        assertThat(UnreadCountManager.getUnreadCount(feed.id), `is`(feed.unreadAriticlesCount))
    }

    @Test
    fun minus1ReturnsWhenGetNotExistRssUnreadCount() {
        assertThat(UnreadCountManager.getUnreadCount(-9999), `is`(-1))
    }

    @Test
    fun countBecomes0WhenReadAll() {
        UnreadCountManager.readAll(feed.id)
        assertThat(UnreadCountManager.getUnreadCount(feed.id), `is`(0))
    }

    @Test
    fun totalDecreasesWhenReadAll() {
        UnreadCountManager.readAll(feed.id)
        assertThat(UnreadCountManager.total, `is`(0))
    }

    @Test
    fun totalDoesNotChangeWhenReadAllInvalidRss() {
        val total = UnreadCountManager.total
        UnreadCountManager.readAll(-9999)
        assertThat(UnreadCountManager.total, `is`(total))
    }

    @Test
    fun totalBecomes0WhenReadAll() {
        UnreadCountManager.readAll()
        assertThat(UnreadCountManager.total, `is`(0))
    }

    @Test
    fun curationCountReturnsWhenGet() {
        val testCurationId = 1
        val testCount = 10
        Mockito.`when`(mockAdapter.calcNumOfAllUnreadArticlesOfCuration(testCurationId)).thenReturn(testCount)
        feed.unreadAriticlesCount = testUnreadCount
        UnreadCountManager.addFeed(feed)
        assertThat(UnreadCountManager.getCurationCount(testCurationId), `is`(testCount))
    }
}