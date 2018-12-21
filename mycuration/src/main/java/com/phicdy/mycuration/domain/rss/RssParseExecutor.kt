package com.phicdy.mycuration.domain.rss

import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.util.UrlUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RssParseExecutor(private val parser: RssParser, private val rssRepository: RssRepository) {

    interface RssParseCallback {
        fun succeeded(rssUrl: String)
        fun failed(reason: RssParseResult.FailedReason, url: String)
    }

    suspend fun start(parseTargetUrl: String, callback: RssParseCallback) = withContext(Dispatchers.Main) {
        val result = withContext(Dispatchers.IO) {
            parser.parseRssXml(UrlUtil.removeUrlParameter(parseTargetUrl), true)
        }
        if (result.failedReason == RssParseResult.FailedReason.NOT_FAILED && result.feed != null) {
            result.feed.run {
                rssRepository.store(title, url, format, siteUrl)
                callback.succeeded(url)
            }
        } else {
            callback.failed(result.failedReason, parseTargetUrl)
        }
    }
}
