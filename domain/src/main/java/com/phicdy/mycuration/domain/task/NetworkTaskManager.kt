package com.phicdy.mycuration.domain.task

import com.phicdy.mycuration.data.network.HatenaBookmarkApi
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.IOException

class NetworkTaskManager(
    private val articleRepository: ArticleRepository,
    private val rssRepository: RssRepository,
    private val curationRepository: CurationRepository,
    private val filterRepository: FilterRepository,
    private val client: OkHttpClient,
    private val parser :RssParser
) {

    val isUpdatingFeed: Boolean get() = false

    suspend fun updateAll(rssList: List<Feed>): List<Feed> =
        rssList.filter { it.id > 0 }
            .map { updateFeed(it) }

    suspend fun updateFeed(feed: Feed): Feed = withContext(Dispatchers.IO) {
        if (feed.url.isEmpty()) return@withContext feed
        Timber.d("start update rss: ${feed.title}")
        try {
            val request = Request.Builder().url(feed.url).build()
            val response = client.newCall(request).execute()
            val inputStream = response.body()?.byteStream() ?: return@withContext feed
            val articles = parser.parseArticlesFromRss(inputStream)
            val storedUrlList = articleRepository.getStoredUrlListIn(articles)
            val newArticleList = articles.filter { it.url !in storedUrlList }
            if (newArticleList.isEmpty()) {
                val size = articleRepository.getUnreadArticleCount(feed.id)
                rssRepository.updateUnreadArticleCount(feed.id, size)
                feed.unreadAriticlesCount = size
                Timber.d("finish update rss ${feed.title}, no update")
                return@withContext feed
            }

            val savedArtices = articleRepository.saveNewArticles(newArticleList, feed.id)
            curationRepository.saveCurationsOf(savedArtices)
            feed.unreadAriticlesCount += newArticleList.size

            newArticleList.forEach {
                launch {
                    val hatenaBookmarkApi = HatenaBookmarkApi()
                    val point = hatenaBookmarkApi.request(it.url)
                    articleRepository.saveHatenaPoint(it.url, point)
                }
            }

            FilterTask(articleRepository, filterRepository).applyFiltering(feed.id)

            val size = articleRepository.getUnreadArticleCount(feed.id)
            rssRepository.updateUnreadArticleCount(feed.id, size)
            feed.unreadAriticlesCount = size
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: IOException) {
            Timber.e(e)
        } catch (e: RuntimeException) {
            Timber.e(e)
        }
        Timber.d("finish update rss ${feed.title}")
        return@withContext feed
    }
}
