package com.phicdy.mycuration.data.repository

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.rss.Feed
import kotlinx.coroutines.experimental.coroutineScope
import kotlinx.coroutines.experimental.launch

class UnreadCountRepository(private val adapter: DatabaseAdapter,
                            private val rssRepository: RssRepository,
                            private val curationRepository: CurationRepository) {
    companion object {
        private const val INIT_TOTAL = -1
    }

    var total = INIT_TOTAL
    private val unreadCountMap = hashMapOf<Int, Int>()

    suspend fun retrieve() = coroutineScope {
        val allFeeds = rssRepository.getAllFeedsWithNumOfUnreadArticles()
        total = 0
        unreadCountMap.clear()
        allFeeds.forEach { feed ->
            unreadCountMap[feed.id] = feed.unreadAriticlesCount
            total += feed.unreadAriticlesCount
        }
    }

    fun deleteFeed(feedId: Int) {
        unreadCountMap[feedId]?.let {
            total -= it
            unreadCountMap.remove(feedId)
        }
    }

    suspend fun conutUpUnreadCount(feedId: Int) = coroutineScope {
        unreadCountMap[feedId]?.let {
            unreadCountMap.put(feedId, it + 1)
            total++
            updateDatbase(feedId)
        }
    }

    suspend fun countDownUnreadCount(feedId: Int) = coroutineScope {
        unreadCountMap[feedId]?.let {
            if (it == 0) return@coroutineScope
            unreadCountMap[feedId] = it - 1
            total--
            updateDatbase(feedId)
        }
    }

    suspend fun getUnreadCount(feedId: Int): Int {
        return unreadCountMap[feedId] ?: -1
    }

    private suspend fun updateDatbase(feedId: Int) = coroutineScope {
        unreadCountMap[feedId]?.let {
            launch {
                adapter.updateUnreadArticleCount(feedId, it)
            }
        }
    }

    suspend fun readAll(feedId: Int) {
        unreadCountMap[feedId]?.let {
            total -= it
            unreadCountMap.put(feedId, 0)
            updateDatbase(feedId)
        }
    }

    suspend fun readAll() = coroutineScope {
        total = 0
        for (id in unreadCountMap.keys) {
            unreadCountMap[id] = 0
            updateDatbase(id)
        }
    }

    suspend fun getCurationCount(curationId: Int): Int {
        return curationRepository.calcNumOfAllUnreadArticlesOfCuration(curationId)
    }
}
