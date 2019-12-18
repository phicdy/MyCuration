package com.phicdy.mycuration

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.rss.IconFetchWorker

class DefaultWorkerFactory(
        private val rssRepository: RssRepository
) : WorkerFactory() {

    override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (Class.forName(workerClassName)) {
            IconFetchWorker::class.java -> IconFetchWorker(appContext, workerParameters, rssRepository)
            else -> throw IllegalArgumentException("invalid worker: $workerClassName")
        }
    }
}