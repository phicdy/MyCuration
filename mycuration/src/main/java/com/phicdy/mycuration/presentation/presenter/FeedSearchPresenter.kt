package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.domain.rss.RssParseExecutor
import com.phicdy.mycuration.domain.rss.RssParseResult
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.domain.rss.UnreadCountManager
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.presentation.view.FeedSearchView
import com.phicdy.mycuration.util.UrlUtil
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class FeedSearchPresenter(private val view: FeedSearchView,
                          private val networkTaskManager: NetworkTaskManager,
                          private val adapter: DatabaseAdapter,
                          private val unreadManager: UnreadCountManager,
                          private val parser: RssParser) : Presenter {
    var callback: RssParseExecutor.RssParseCallback = object : RssParseExecutor.RssParseCallback {
        override fun succeeded(rssUrl: String) {
            onFinishAddFeed(rssUrl, RssParseResult.FailedReason.NOT_FAILED)
        }

        override fun failed(reason: RssParseResult.FailedReason, url: String) {
            onFinishAddFeed(url, reason)
        }
    }

    override fun create() {}

    override fun resume() {}

    override fun pause() {}

    fun onFabClicked(url: String) {
        if (url == "") return
        view.startFeedUrlHookActivity(url)
    }

    fun handle(query: String) {
        if (UrlUtil.isCorrectUrl(query)) {
            view.showProgressBar()
            val executor = RssParseExecutor(parser, adapter)
            executor.start(query, callback)
            return
        }
        try {
            val encodedQuery = URLEncoder.encode(query, "utf-8")
            val url = "https://www.google.co.jp/search?q=$encodedQuery"
            view.load(url)
            view.setSearchViewTextFrom(url)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    fun onFinishAddFeed(url: String, reason: RssParseResult.FailedReason) {
        val newFeed = adapter.getFeedByUrl(url)
        if (reason === RssParseResult.FailedReason.NOT_FAILED && newFeed != null) {
            unreadManager.addFeed(newFeed)
            networkTaskManager.updateFeed(newFeed)
            view.showAddFeedSuccessToast()
        } else {
            if (reason === RssParseResult.FailedReason.INVALID_URL) {
                view.showInvalidUrlErrorToast()
            } else {
                view.showGenericErrorToast()
            }
            view.trackFailedUrl(url)
        }
        view.dismissProgressBar()
        view.finishView()
    }
}
