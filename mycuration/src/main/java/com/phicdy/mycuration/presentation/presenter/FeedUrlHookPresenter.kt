package com.phicdy.mycuration.presentation.presenter

import android.content.Intent
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.domain.rss.RssParseExecutor
import com.phicdy.mycuration.domain.rss.RssParseResult
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.domain.rss.UnreadCountManager
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.presentation.view.FeedUrlHookView
import com.phicdy.mycuration.util.UrlUtil
import kotlinx.coroutines.experimental.runBlocking

class FeedUrlHookPresenter(private val view: FeedUrlHookView,
                           private val action: String,
                           private val dataString: String,
                           private val extrasText: CharSequence,
                           private val dbAdapter: DatabaseAdapter,
                           private val unreadCountManager: UnreadCountManager,
                           private val networkTaskManager: NetworkTaskManager,
                           private val parser: RssParser) {

    var callback: RssParseExecutor.RssParseCallback = object : RssParseExecutor.RssParseCallback {
        override fun succeeded(rssUrl: String) = runBlocking {
            val newFeed = dbAdapter.getFeedByUrl(rssUrl)
            unreadCountManager.addFeed(newFeed)
            networkTaskManager.updateFeed(newFeed)
            view.showSuccessToast()
            view.finishView()
        }

        override fun failed(reason: RssParseResult.FailedReason, url: String) {
            if (reason === RssParseResult.FailedReason.INVALID_URL) {
                view.showInvalidUrlErrorToast()
            } else {
                view.showGenericErrorToast()
            }
            view.trackFailedUrl(url)
            view.finishView()
        }
    }

    fun create() {
        if (action != Intent.ACTION_VIEW && action != Intent.ACTION_SEND) {
            view.finishView()
            return
        }
        var url: String? = null
        if (action == Intent.ACTION_VIEW) {
            url = dataString
        } else if (action == Intent.ACTION_SEND) {
            // For Chrome
            url = extrasText.toString()
        }
        if (url != null) {
            handle(action, url)
        }
    }

    private fun handle(action: String, url: String) {
        if (action == Intent.ACTION_VIEW || action == Intent.ACTION_SEND) {
            if (UrlUtil.isCorrectUrl(url)) {
                val executor = RssParseExecutor(parser, dbAdapter)
                executor.start(url, callback)
            } else {
                view.showInvalidUrlErrorToast()
                view.trackFailedUrl(url)
            }
        } else {
            view.finishView()
        }
    }
}
