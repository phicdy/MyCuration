package com.phicdy.mycuration.domain.rss

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.util.UrlUtil
import java.util.concurrent.Executors

class RssParseExecutor(private val parser: RssParser, private val adapter: DatabaseAdapter) {

    interface RssParseCallback {
        fun succeeded(rssUrl: String)
        fun failed(@RssParseResult.FailedReason reason: Int, url: String)
    }

    fun start(parseTargetUrl: String, callback: RssParseCallback) {
        executorService.execute {
            parser.parseRssXml(UrlUtil.removeUrlParameter(parseTargetUrl), true).let { result ->
                if (result.failedReason == RssParseResult.NOT_FAILED && result.feed != null) {
                    result.feed.run {
                        adapter.saveNewFeed(title, url, format, siteUrl)
                        callback.succeeded(url)
                    }
                } else {
                    callback.failed(result.failedReason, parseTargetUrl)
                }
            }
        }
    }

    companion object {
        private val executorService = Executors.newSingleThreadExecutor()
    }
}
