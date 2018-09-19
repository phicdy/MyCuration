package com.phicdy.mycuration.domain.rss

import android.support.annotation.VisibleForTesting
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.rss.Feed

object UnreadCountManager {
    var total = 0
        private set
    private val unreadCountMap = hashMapOf<Int, Int>()
    private var adapter: DatabaseAdapter

    @VisibleForTesting
    fun inject(adapter: DatabaseAdapter) {
        this.adapter = adapter
    }

    init {
        total = 0
        adapter = DatabaseAdapter.getInstance()
        val allFeeds = adapter.allFeedsWithNumOfUnreadArticles
        synchronized(unreadCountMap) {
            unreadCountMap.clear()
            allFeeds.forEach {
                unreadCountMap[it.id] = it.unreadAriticlesCount
                total += it.unreadAriticlesCount
            }
        }
    }

    fun clear() {
        total = 0
        unreadCountMap.clear()
    }

    fun addFeed(feed: Feed?) {
        if (feed == null) {
            return
        }
        unreadCountMap[feed.id] = feed.unreadAriticlesCount
        total += feed.unreadAriticlesCount
    }

    fun deleteFeed(feedId: Int) {
        if (!unreadCountMap.containsKey(feedId)) return
        total -= unreadCountMap[feedId]!!
        unreadCountMap.remove(feedId)
    }

    fun conutUpUnreadCount(feedId: Int) {
        synchronized(unreadCountMap) {
            if (!unreadCountMap.containsKey(feedId)) return
            val count = unreadCountMap[feedId]!! + 1
            unreadCountMap.put(feedId, count)
        }
        total++
        updateDatbase(feedId)
    }

    fun countDownUnreadCount(feedId: Int) {
        synchronized(unreadCountMap) {
            if (!unreadCountMap.containsKey(feedId)) return
            unreadCountMap[feedId]!!.also { count ->
                if (count == 0) return
                unreadCountMap[feedId] = count - 1
            }
        }
        total--
        updateDatbase(feedId)
    }

    fun getUnreadCount(feedId: Int): Int {
        return if (unreadCountMap.containsKey(feedId)) unreadCountMap[feedId]!! else -1
    }

    private fun updateDatbase(feedId: Int) {
        if (!unreadCountMap.containsKey(feedId)) return

        synchronized(unreadCountMap) {
            val count = unreadCountMap[feedId]!!
            object : Thread() {
                override fun run() {
                    adapter.updateUnreadArticleCount(feedId, count)
                }
            }.start()
        }
    }

    fun readAll(feedId: Int) {
        synchronized(unreadCountMap) {
            if (!unreadCountMap.containsKey(feedId)) return
            val count = unreadCountMap[feedId]!!
            total -= count
            unreadCountMap.put(feedId, 0)
        }
        updateDatbase(feedId)
    }

    fun readAll() {
        synchronized(unreadCountMap) {
            total = 0
            for (id in unreadCountMap.keys) {
                unreadCountMap[id] = 0
                updateDatbase(id)
            }
        }
    }

    fun refreshConut(feedId: Int) {
        synchronized(unreadCountMap) {
            // Decrease original unread count
            if (!unreadCountMap.containsKey(feedId)) return
            val oldCount = unreadCountMap[feedId]!!
            total -= oldCount

            // Calc unread count from database
            val count = adapter.getNumOfUnreadArtilces(feedId)
            adapter.updateUnreadArticleCount(feedId, count)
            unreadCountMap[feedId] = count
            total += count
        }
    }

    fun getCurationCount(curationId: Int): Int {
        return adapter.calcNumOfAllUnreadArticlesOfCuration(curationId)
    }
}
