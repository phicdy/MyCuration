package com.phicdy.mycuration.domain.rss

import com.phicdy.mycuration.domain.entity.Feed


class RssParseResult(val feed: Feed? = null,
                     val failedReason: FailedReason = FailedReason.NOT_FAILED) {

    enum class FailedReason {
        NOT_FAILED,
        INVALID_URL,
        NON_RSS_HTML,
        NOT_FOUND
    }
}
