package com.phicdy.mycuration.domain.task

import com.phicdy.mycuration.data.network.HatenaBookmarkApi
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.io.IOException

class NetworkTaskManager(
        private val articleRepository: ArticleRepository,
        private val rssRepository: RssRepository,
        private val curationRepository: CurationRepository,
        private val filterRepository: FilterRepository
) : KoinComponent {

    val isUpdatingFeed: Boolean get() = false

    private val client: OkHttpClient by inject()

    suspend fun updateAll(rssList: List<Feed>) = withContext(Dispatchers.IO) {
        rssList.filter { it.id > 0 }
                .map { async { updateFeed(it) } }
                .map { it.await() }
    }

    suspend fun updateFeed(feed: Feed) = coroutineScope {
        if (feed.url.isEmpty()) return@coroutineScope
        try {
            val request = Request.Builder().url(feed.url).build()
            val response = client.newCall(request).execute()
            val inputStream = response.body()?.byteStream() ?: return@coroutineScope
            val parser = RssParser()
            val articles = parser.parseArticlesFromRss(inputStream)
            val storedUrlList = articleRepository.getStoredUrlListIn(articles)
            articles.filter { it.url !in storedUrlList }
                    .also {
                        val savedArtices = articleRepository.saveNewArticles(it, feed.id)
                        curationRepository.saveCurationsOf(savedArtices)
                        feed.unreadAriticlesCount += it.size
                        rssRepository.updateUnreadArticleCount(feed.id, feed.unreadAriticlesCount + it.size)
                    }
                    .map {
                        val hatenaBookmarkApi = HatenaBookmarkApi()
                        val point = hatenaBookmarkApi.request(it.url)
                        articleRepository.saveHatenaPoint(it.url, point)
                    }

            val updatedCount = FilterTask(articleRepository, filterRepository).applyFiltering(feed.id)
            rssRepository.updateUnreadArticleCount(feed.id, feed.unreadAriticlesCount - updatedCount)
            feed.unreadAriticlesCount -= updatedCount
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (feed.iconPath == Feed.DEDAULT_ICON_PATH) {
                val iconUrl = GetFeedIconTask().execute(feed.siteUrl)
                rssRepository.saveIconPath(feed.siteUrl, iconUrl)
            }
        } catch (e: IOException) {
            Timber.e(e)
        } catch (e: RuntimeException) {
            Timber.e(e)
        }
    }
}
