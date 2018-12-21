package com.phicdy.mycuration.domain.task

import com.phicdy.mycuration.domain.rss.IconParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetFeedIconTask {

    suspend fun execute(siteUrl: String): String = withContext(Dispatchers.IO) {
        return@withContext IconParser().parseHtml(siteUrl)
    }
}
