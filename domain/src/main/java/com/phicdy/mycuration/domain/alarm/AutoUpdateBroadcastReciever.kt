package com.phicdy.mycuration.domain.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.phicdy.mycuration.data.network.HatenaBookmarkApi
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.domain.util.NetworkUtil
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class AutoUpdateBroadcastReciever : BroadcastReceiver(), KoinComponent {

    private val rssRepository: RssRepository by inject()
    private val articleRepository: ArticleRepository by inject()
    private val networkTaskManager: NetworkTaskManager by inject()

    override fun onReceive(context: Context, intent: Intent?) {
        GlobalScope.launch {
            when (intent?.action) {
                AUTO_UPDATE_ACTION -> handleAutoUpdate(context)
                AUTO_UPDATE_HATENA_ACTION -> handleUpdateHatena(context)
                FIX_UNREAD_COUNT_ACTION -> handleFixUnreadCount(context)
            }
        }
    }

    private suspend fun handleAutoUpdate(context: Context) = coroutineScope {
        val feeds = rssRepository.getAllFeedsWithNumOfUnreadArticles()
        networkTaskManager.updateAll(feeds)
        val manager = AlarmManagerTaskManager(context)
        manager.setNewHatenaUpdateAlarmAfterFeedUpdate(context)

        // Save new time
        manager.setNewAlarm(PreferenceHelper.autoUpdateIntervalSecond)
    }

    private suspend fun handleUpdateHatena(context: Context) {
        // Update Hatena point
        val feeds = rssRepository.getAllFeedsWithNumOfUnreadArticles()
        if (feeds.isEmpty()) return

        // Update has higher priority
        if (networkTaskManager.isUpdatingFeed) {
            AlarmManagerTaskManager(context).setNewHatenaUpdateAlarmAfterFeedUpdate(context)
            return
        }

        val isWifiConnected = NetworkUtil.isWifiConnected(context)
        val hatenaBookmarkApi = HatenaBookmarkApi()
        var delaySec = 0
        var totalNum = 0
        for (feed in feeds) {
            val unreadArticles = articleRepository.getUnreadArticlesOfRss(feed.id, true)
            if (unreadArticles.isEmpty()) continue
            for (unreadArticle in unreadArticles) {
                if (unreadArticle.point != Article.DEDAULT_HATENA_POINT && !isWifiConnected) continue
                totalNum++
                val point = hatenaBookmarkApi.request(unreadArticle.url)
                articleRepository.saveHatenaPoint(unreadArticle.url, point)
                if (totalNum % 10 == 0) delaySec += 2
            }
        }
    }

    private suspend fun handleFixUnreadCount(context: Context) {
        val rssList = rssRepository.getAllFeedsWithoutNumOfUnreadArticles()
        rssList.forEach {
            val size = articleRepository.getUnreadArticlesOfRss(it.id, false).size
            rssRepository.updateUnreadArticleCount(it.id, size)
        }

        val manager = AlarmManagerTaskManager(context)
        manager.setFixUnreadCountAlarm()
    }

    companion object {
        const val AUTO_UPDATE_ACTION = "autoUpdateFeed"
        const val AUTO_UPDATE_HATENA_ACTION = "autoUpdateHatena"
        const val FIX_UNREAD_COUNT_ACTION = "fixUnreadCount"
    }
}
