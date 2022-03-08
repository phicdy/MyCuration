package com.phicdy.mycuration.feedurlhook

import android.content.Intent
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.rss.RssParseExecutor
import com.phicdy.mycuration.domain.rss.RssParseResult
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.domain.rss.RssUrlHookIntentData
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.util.UrlUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class FeedUrlHookPresenter @Inject constructor(
    private val view: FeedUrlHookView,
    private val rssUrlHookIntentData: RssUrlHookIntentData,
    private val rssRepository: RssRepository,
    private val networkTaskManager: NetworkTaskManager,
    private val coroutineScope: CoroutineScope,
    private val parser: RssParser
) {

    var callback: RssParseExecutor.RssParseCallback = object : RssParseExecutor.RssParseCallback {
        override fun succeeded(rssUrl: String) {
            coroutineScope.launch {
                val newFeed = rssRepository.getFeedByUrl(rssUrl)
                newFeed?.let {
                    networkTaskManager.updateFeed(newFeed)
                }
                view.showSuccessToast()
                view.finishView()
            }
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

    suspend fun create() {
        if (rssUrlHookIntentData.action != Intent.ACTION_VIEW && rssUrlHookIntentData.action != Intent.ACTION_SEND) {
            view.finishView()
            return
        }
        var url: String? = null
        if (rssUrlHookIntentData.action == Intent.ACTION_VIEW) {
            url = rssUrlHookIntentData.dataString
        } else if (rssUrlHookIntentData.action == Intent.ACTION_SEND) {
            // For Chrome
            url = rssUrlHookIntentData.extrasText.toString()
        }
        if (url != null) {
            handle(rssUrlHookIntentData.action, url)
        }
    }

    private suspend fun handle(action: String, url: String) {
        if (action == Intent.ACTION_VIEW || action == Intent.ACTION_SEND) {
            if (UrlUtil.isCorrectUrl(url)) {
                val executor = RssParseExecutor(parser, rssRepository)
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
