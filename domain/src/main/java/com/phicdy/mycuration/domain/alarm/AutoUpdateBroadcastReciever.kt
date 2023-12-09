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
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AutoUpdateBroadcastReciever : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AutoUpdateEntryPoint {
        fun provideRssRepository(): RssRepository
        fun provideArticleRepository(): ArticleRepository
        fun provideNetworkTaskManager(): NetworkTaskManager
    }

    private lateinit var hiltEntryPoint: AutoUpdateEntryPoint

    override fun onReceive(context: Context, intent: Intent?) {
        val appContext = context.applicationContext
        hiltEntryPoint = EntryPointAccessors.fromApplication(appContext, AutoUpdateEntryPoint::class.java)

        GlobalScope.launch {
            when (intent?.action) {
                AUTO_UPDATE_ACTION -> handleAutoUpdate(context)
                AUTO_UPDATE_HATENA_ACTION -> handleUpdateHatena(context)
                FIX_UNREAD_COUNT_ACTION -> handleFixUnreadCount(context)
            }
        }
    }

    private suspend fun handleAutoUpdate(context: Context) = coroutineScope {
        val feeds = hiltEntryPoint.provideRssRepository().getAllFeeds()
        hiltEntryPoint.provideNetworkTaskManager().updateAll(feeds)
        val manager = AlarmManagerTaskManager(context)
        manager.setNewHatenaUpdateAlarmAfterFeedUpdate(context)

        // Save new time
        manager.setNewAlarm(PreferenceHelper.autoUpdateIntervalSecond)
    }

    private suspend fun handleUpdateHatena(context: Context) {
        // Update Hatena point
        val feeds = hiltEntryPoint.provideRssRepository().getAllFeeds()
        if (feeds.isEmpty()) return

        // Update has higher priority
        if (hiltEntryPoint.provideNetworkTaskManager().isUpdatingFeed) {
            AlarmManagerTaskManager(context).setNewHatenaUpdateAlarmAfterFeedUpdate(context)
            return
        }

        val isWifiConnected = NetworkUtil.isWifiConnected(context)
        val hatenaBookmarkApi = HatenaBookmarkApi()
        var delaySec = 0
        var totalNum = 0
        for (feed in feeds) {
            val unreadArticles = hiltEntryPoint.provideArticleRepository().getUnreadArticlesOfRss(feed.id, true)
            if (unreadArticles.isEmpty()) continue
            for (unreadArticle in unreadArticles) {
                if (unreadArticle.point != Article.DEDAULT_HATENA_POINT && !isWifiConnected) continue
                totalNum++
                val point = hatenaBookmarkApi.request(unreadArticle.url)
                hiltEntryPoint.provideArticleRepository().saveHatenaPoint(unreadArticle.url, point)
                if (totalNum % 10 == 0) delaySec += 2
            }
        }
    }

    private suspend fun handleFixUnreadCount(context: Context) {
        val rssList = hiltEntryPoint.provideRssRepository().getAllFeeds()
        rssList.forEach {
            val size = hiltEntryPoint.provideArticleRepository().getUnreadArticlesOfRss(it.id, false).size
            hiltEntryPoint.provideRssRepository().updateUnreadArticleCount(it.id, size)
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
