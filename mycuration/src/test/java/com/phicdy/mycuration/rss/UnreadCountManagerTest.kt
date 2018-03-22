package com.phicdy.mycuration.rss


import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.data.rss.UnreadCountManager
import com.phicdy.mycuration.db.DatabaseAdapter
import com.phicdy.mycuration.db.DatabaseHelper

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import java.util.ArrayList

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat

class UnreadCountManagerTest {

    private lateinit var mockAdapter: DatabaseAdapter
    private lateinit var unreadCountManager: UnreadCountManager
    private val feed = Feed()

    companion object {
        private const val testUnreadCount = 5
    }

    @Before
    fun setUp() {
        DatabaseAdapter.setUp(Mockito.mock(DatabaseHelper::class.java))
        mockAdapter = Mockito.mock(DatabaseAdapter::class.java)
        val allFeedList = ArrayList<Feed>()
        Mockito.`when`(mockAdapter.allFeedsWithNumOfUnreadArticles).thenReturn(allFeedList)
        unreadCountManager = UnreadCountManager.newInstance(mockAdapter)
        feed.unreadAriticlesCount = testUnreadCount
        unreadCountManager.addFeed(feed)
    }

    @After
    fun tearDown() {
        unreadCountManager.clear()
    }

    @Test
    fun totalIncreasesWhenRssIsAdded() {
        val currentTotal = unreadCountManager.total
        assertThat(currentTotal, `is`(testUnreadCount))
    }

    @Test
    fun totalDoesNotChangeWhenNullIsAdded() {
        val total = unreadCountManager.total
        unreadCountManager.addFeed(null)
        val currentTotal = unreadCountManager.total
        assertThat(currentTotal, `is`(total))
    }

    @Test
    fun totalDecreasesWhenRssIsDeleted() {
        unreadCountManager.deleteFeed(feed.id)
        assertThat(unreadCountManager.total, `is`(0))
    }

    @Test
    fun totalDoesNotChangeWhenInvalidRssIsDeleted() {
        unreadCountManager.deleteFeed(-9999)
        val currentTotal = unreadCountManager.total
        assertThat(currentTotal, `is`(testUnreadCount))
    }

    @Test
    fun totalIncreasesWhenCountUp() {
        unreadCountManager.conutUpUnreadCount(feed.id)
        val currentTotal = unreadCountManager.total
        assertThat(currentTotal, `is`(testUnreadCount + 1))
    }

    @Test
    fun totalDoesNotChangeWhenInvalidCountUp() {
        val total = unreadCountManager.total
        unreadCountManager.conutUpUnreadCount(-9999)
        val currentTotal = unreadCountManager.total
        assertThat(currentTotal, `is`(total))
    }

    @Test
    fun totalDecreasesWhenCountDown() {
        unreadCountManager.countDownUnreadCount(feed.id)
        val currentTotal = unreadCountManager.total
        assertThat(currentTotal, `is`(testUnreadCount - 1))
    }

    @Test
    fun totalDoesNotChangeWhenInvalidCountDown() {
        val total = unreadCountManager.total
        unreadCountManager.countDownUnreadCount(-9999)
        val currentTotal = unreadCountManager.total
        assertThat(currentTotal, `is`(total))
    }

    @Test
    fun totalKeeps0WhenCountDown() {
        for (i in 0 until feed.unreadAriticlesCount) {
            unreadCountManager.countDownUnreadCount(feed.id)
        }
        unreadCountManager.countDownUnreadCount(feed.id)
        val currentTotal = unreadCountManager.total
        assertThat(currentTotal, `is`(0))
    }

    @Test
    fun unreadCountIsSameWithOneOfRss() {
        assertThat(unreadCountManager.getUnreadCount(feed.id), `is`(feed.unreadAriticlesCount))
    }

    @Test
    fun minus1ReturnsWhenGetNotExistRssUnreadCount() {
        assertThat(unreadCountManager.getUnreadCount(-9999), `is`(-1))
    }

    @Test
    fun countBecomes0WhenReadAll() {
        unreadCountManager.readAll(feed.id)
        assertThat(unreadCountManager.getUnreadCount(feed.id), `is`(0))
    }

    @Test
    fun totalDecreasesWhenReadAll() {
        unreadCountManager.readAll(feed.id)
        assertThat(unreadCountManager.total, `is`(0))
    }

    @Test
    fun totalDoesNotChangeWhenReadAllInvalidRss() {
        val total = unreadCountManager.total
        unreadCountManager.readAll(-9999)
        assertThat(unreadCountManager.total, `is`(total))
    }

    @Test
    fun totalBecomes0WhenReadAll() {
        unreadCountManager.readAll()
        assertThat(unreadCountManager.total, `is`(0))
    }

    @Test
    fun totalRefreshesWhenRefreshRss() {
        val newUnreadCount = 10
        Mockito.`when`(mockAdapter.getNumOfUnreadArtilces(feed.id)).thenReturn(newUnreadCount)
        unreadCountManager = UnreadCountManager.newInstance(mockAdapter)
        feed.unreadAriticlesCount = testUnreadCount
        unreadCountManager.addFeed(feed)
        unreadCountManager.refreshConut(feed.id)
        assertThat(unreadCountManager.total, `is`(newUnreadCount))
    }

    @Test
    fun countRefreshesWhenRefreshRss() {
        val newUnreadCount = 10
        Mockito.`when`(mockAdapter.getNumOfUnreadArtilces(feed.id)).thenReturn(newUnreadCount)
        unreadCountManager = UnreadCountManager.newInstance(mockAdapter)
        feed.unreadAriticlesCount = testUnreadCount
        unreadCountManager.addFeed(feed)
        unreadCountManager.refreshConut(feed.id)
        assertThat(unreadCountManager.getUnreadCount(feed.id), `is`(newUnreadCount))
    }

    @Test
    fun totalDoesNotChangeWhenRefreshInvalidRss() {
        val total = unreadCountManager.total
        unreadCountManager.refreshConut(-9999)
        Mockito.`when`(mockAdapter.getNumOfUnreadArtilces(-9999)).thenReturn(10)
        unreadCountManager = UnreadCountManager.newInstance(mockAdapter)
        feed.unreadAriticlesCount = testUnreadCount
        unreadCountManager.addFeed(feed)
        assertThat(unreadCountManager.total, `is`(total))
    }

    @Test
    fun curationCountReturnsWhenGet() {
        val testCurationId = 1
        val testCount = 10
        Mockito.`when`(mockAdapter.calcNumOfAllUnreadArticlesOfCuration(testCurationId)).thenReturn(testCount)
        unreadCountManager = UnreadCountManager.newInstance(mockAdapter)
        feed.unreadAriticlesCount = testUnreadCount
        unreadCountManager.addFeed(feed)
        assertThat(unreadCountManager.getCurationCount(testCurationId), `is`(testCount))
    }
}