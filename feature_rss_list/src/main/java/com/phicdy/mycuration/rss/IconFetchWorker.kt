package com.phicdy.mycuration.rss

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.task.GetFeedIconTask
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IconFetchWorker(
        appContext: Context,
        workerParams: WorkerParameters,
        private val rssRepository: RssRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        rssRepository.getAllFeedsWithoutNumOfUnreadArticles().forEach {
            if (it.iconPath.isEmpty() || it.iconPath == Feed.DEDAULT_ICON_PATH) {
                val iconUrl = GetFeedIconTask().execute(it.siteUrl)
                rssRepository.saveIconPath(it.siteUrl, iconUrl)
            }
        }
        return@withContext Result.success()
    }
}