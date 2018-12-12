package com.phicdy.mycuration.domain.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.data.rss.Article
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.data.network.HatenaBookmarkApi
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.util.NetworkUtil
import com.phicdy.mycuration.util.PreferenceHelper
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class AutoUpdateBroadcastReciever : BroadcastReceiver(), KoinComponent {

    private val rssRepository: RssRepository by inject()
    private val articleRepository: ArticleRepository by inject()
    private val networkTaskManager: NetworkTaskManager by inject()

    override fun onReceive(context: Context, intent: Intent?) {
        GlobalScope.launch {
            if (intent == null || intent.action == null) return@launch
            if (intent.action == AUTO_UPDATE_ACTION) {
                handleAutoUpdate(context)
            } else if (intent.action == AUTO_UPDATE_HATENA_ACTION) {
                handleUpdateHatena(context)
            }
        }
    }

    private suspend fun handleAutoUpdate(context: Context) = coroutineScope {
        val feeds = rssRepository.getAllFeedsWithoutNumOfUnreadArticles()
        networkTaskManager.updateAllFeeds(feeds)
                .observeOn(Schedulers.io())
                .subscribe(object : Subscriber<Feed> {
                    override fun onSubscribe(s: Subscription) {
                        s.request(feeds.size.toLong())
                    }

                    override fun onNext(feed: Feed) {}

                    override fun onError(t: Throwable) {}

                    override fun onComplete() {}
                })
        val manager = AlarmManagerTaskManager(context)
        manager.setNewHatenaUpdateAlarmAfterFeedUpdate(context)

        // Save new time
        manager.setNewAlarm(PreferenceHelper.autoUpdateIntervalSecond)
    }

    private suspend fun handleUpdateHatena(context: Context) {
        val dbAdapter = DatabaseAdapter.getInstance()
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

    companion object {
        const val AUTO_UPDATE_ACTION = "autoUpdateFeed"
        const val AUTO_UPDATE_HATENA_ACTION = "autoUpdateHatena"
    }
}
