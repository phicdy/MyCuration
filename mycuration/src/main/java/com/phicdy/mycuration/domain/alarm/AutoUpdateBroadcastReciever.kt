package com.phicdy.mycuration.domain.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.rss.Article
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.domain.task.GetHatenaBookmark
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.util.NetworkUtil
import com.phicdy.mycuration.util.PreferenceHelper
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class AutoUpdateBroadcastReciever : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null || intent.action == null) return
        val dbAdapter = DatabaseAdapter.getInstance()
        if (intent.action == AUTO_UPDATE_ACTION) {
            val updateTask = NetworkTaskManager
            val feeds = dbAdapter.allFeedsWithoutNumOfUnreadArticles
            updateTask.updateAllFeeds(feeds)
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
        } else if (intent.action == AUTO_UPDATE_HATENA_ACTION) {
            // Update Hatena point
            val feeds = dbAdapter.allFeedsWithNumOfUnreadArticles
            if (feeds.isEmpty()) return

            // Update has higher priority
            if (NetworkTaskManager.isUpdatingFeed) {
                AlarmManagerTaskManager(context).setNewHatenaUpdateAlarmAfterFeedUpdate(context)
                return
            }

            val isWifiConnected = NetworkUtil.isWifiConnected(context)
            val getHatenaBookmark = GetHatenaBookmark(dbAdapter)
            var delaySec = 0
            var totalNum = 0
            for (feed in feeds) {
                val unreadArticles = dbAdapter.getUnreadArticlesInAFeed(feed.id, true)
                if (unreadArticles.isEmpty()) continue
                for (unreadArticle in unreadArticles) {
                    if (unreadArticle.point != Article.DEDAULT_HATENA_POINT && !isWifiConnected) continue
                    totalNum++
                    getHatenaBookmark.request(unreadArticle.url, delaySec)
                    if (totalNum % 10 == 0) delaySec += 2
                }
            }
        }
    }

    companion object {
        const val AUTO_UPDATE_ACTION = "autoUpdateFeed"
        const val AUTO_UPDATE_HATENA_ACTION = "autoUpdateHatena"
    }
}
