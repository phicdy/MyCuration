package com.phicdy.mycuration.data.repository

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class UnreadCountRepository(private val rssRepository: RssRepository,
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
        Timber.d("Retrieve unread count map, %s", unreadCountMap.toString())
    }

    fun deleteFeed(feedId: Int) {
        unreadCountMap[feedId]?.let {
            total -= it
            unreadCountMap.remove(feedId)
        }
    }

    suspend fun conutUpUnreadCount(feedId: Int) = coroutineScope {
        unreadCountMap[feedId]?.let {
            Timber.d("Start count up unread count. Current unread count is %s. RSS ID is %s", it, feedId)
            unreadCountMap[feedId] = it + 1
            total++
            updateDatbase(feedId)
        }
    }

    suspend fun countDownUnreadCount(feedId: Int) = coroutineScope {
        unreadCountMap[feedId]?.let {
            Timber.d("Start count down unread count. Current unread count is %s. RSS ID is %s", it, feedId)
            if (it == 0) return@coroutineScope
            unreadCountMap[feedId] = it - 1
            total--
            updateDatbase(feedId)
        }
    }

    fun getUnreadCount(feedId: Int): Int {
        return unreadCountMap[feedId] ?: -1
    }

    private suspend fun updateDatbase(feedId: Int) = coroutineScope {
        unreadCountMap[feedId]?.let {
            launch {
                Timber.d("Before update, %s", unreadCountMap.toString())
                rssRepository.updateUnreadArticleCount(feedId, it)
            }
        }
    }

    suspend fun readAll(feedId: Int) {
        unreadCountMap[feedId]?.let {
            Timber.d("Start read all. RSS ID is %s", feedId)
            total -= it
            unreadCountMap[feedId] = 0
            updateDatbase(feedId)
        }
    }

    suspend fun readAll() = coroutineScope {
        total = 0
        Timber.d("Start read all. RSS ID is all")
        for (id in unreadCountMap.keys) {
            Timber.d("In read all loop. RSS ID is %s", id)
            unreadCountMap[id] = 0
            updateDatbase(id)
        }
    }

    suspend fun getCurationCount(curationId: Int): Int {
        return curationRepository.calcNumOfAllUnreadArticlesOfCuration(curationId)
    }

    suspend fun appendUnreadArticleCount(rssId: Int, count: Int) {
        if (unreadCountMap.containsKey(rssId)) {
            Timber.d("Append unread article count %s to %s. RSS ID is %s", count, unreadCountMap[rssId], rssId)
            unreadCountMap[rssId] = unreadCountMap[rssId]!! + count
        } else {
            Timber.d("Not contained RSS ID: %s", rssId)
            Timber.d("Append unread article count %s to 0. RSS ID is %s", count, rssId)
            unreadCountMap[rssId] = count
        }
        total += count
        updateDatbase(rssId)
    }

    suspend fun decreaseCount(rssId: Int, count: Int) {
        if (!unreadCountMap.containsKey(rssId)) {
            Timber.d("RSS doesn't exist in the map, skip. RSS ID is %s", rssId)
            return
        }
        if (count <= 0) {
            Timber.d("Decrease count is 0 or less, skip. RSS ID is %s", rssId)
            return
        }
        Timber.d("Decrease unread article count %s. Current count is %s. RSS ID is %s", count, unreadCountMap[rssId], rssId)
        if (unreadCountMap[rssId]!! - count < 0) {
            Timber.d("As the result, count is under 0, set 0")
            total -= unreadCountMap[rssId]!!
            unreadCountMap[rssId] = 0
        } else {
            Timber.d("As the result, count is %s", unreadCountMap[rssId]!! - count)
            total -= count
            unreadCountMap[rssId] = unreadCountMap[rssId]!! - count
        }
        updateDatbase(rssId)
    }
}
