package com.phicdy.mycuration.domain.task

import com.phicdy.mycuration.core.CoroutineDispatcherProvider
import com.phicdy.mycuration.data.network.HatenaBookmarkApi
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    private val parser: RssParser,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val applicationCoroutineScope: CoroutineScope
) {

    val isUpdatingFeed: Boolean get() = false

    suspend fun updateAll(rssList: List<Feed>): List<Feed> =
        withContext(coroutineDispatcherProvider.io()) {
            if (rssList.isEmpty()) return@withContext rssList

            val now = System.currentTimeMillis()
            val deferredList = rssList.map {
                async {
                    val (time, result) = measureTimeMillsWithResult {
                        updateRss(it)
                    }
                    Timber.d("updateRss ${it.title} time: $time")
                    result
                }
            }
            val result = deferredList.awaitAll().flatten()
            val time = System.currentTimeMillis() - now
            Timber.d("update all RSS ${rssList.size} time: $time")

            val (filterTime, filtered) = measureTimeMillsWithResult {
                FilterTask(articleRepository, filterRepository).applyFiltering(result)
            }
            Timber.d("filter ${rssList.size} time: $filterTime")

            val (saveArticlesTime, savedArtices) = measureTimeMillsWithResult {
                articleRepository.saveNewArticles(filtered)
            }
            Timber.d("save articles ${rssList.size} time: $saveArticlesTime")

            val saveCurationTime = measureTimeMillsWithResult {
                curationRepository.saveCurationsOf(savedArtices)
            }
            Timber.d("save curations ${rssList.size} time: $saveCurationTime")

            val refreshUnreadCountTime = measureTimeMillsWithResult {
                val allUnradArticles = articleRepository.getAllUnreadArticles(true)
                for (rss in rssList) {
                    rss.unreadAriticlesCount = allUnradArticles.count { it.feedId == rss.id }
                    rssRepository.updateUnreadArticleCount(rss.id, rss.unreadAriticlesCount)
                }
            }
            Timber.d("refresh unread count ${rssList.size} time: $refreshUnreadCountTime")

            savedArtices.forEach { article ->
                applicationCoroutineScope.launch {
                    val hatenaBookmarkApi = HatenaBookmarkApi()
                    val point = hatenaBookmarkApi.request(article.url)
                    articleRepository.saveHatenaPoint(article.url, point)
                }
            }

            return@withContext rssList
        }

    private suspend fun updateRss(feed: Feed): List<Article> {
        Timber.d("start update rss: ${feed.title}")
        if (feed.url.isEmpty() || feed.id < 0) return emptyList()
        try {
            val request = Request.Builder().url(feed.url).build()
            val response = client.newCall(request).execute()
            val inputStream = response.body()?.byteStream() ?: return emptyList()
            inputStream.use {
                val (parseTime, articles) = measureTimeMillsWithResult {
                    parser.parseArticlesFromRss(inputStream)
                }
                Timber.d("parse ${feed.title} time: $parseTime")
                if (articles.isEmpty()) return emptyList()

                val (getStoredUrlTime, storedUrlList) = measureTimeMillsWithResult {
                    articleRepository.getStoredUrlListIn(articles)
                }
                Timber.d("get stored URL ${feed.title} time: $getStoredUrlTime")
                return articles.filter { it.url !in storedUrlList }
                    .map { it.copy(feedId = feed.id) }
            }
        } catch (e: IOException) {
            Timber.e(e)
        } catch (e: RuntimeException) {
            Timber.e(e)
        }
        return emptyList()
    }

    suspend fun updateFeed(feed: Feed): Feed = withContext(coroutineDispatcherProvider.io()) {
        if (feed.url.isEmpty()) return@withContext feed
        Timber.d("start update rss: ${feed.title}")
        try {
            val request = Request.Builder().url(feed.url).build()
            val response = client.newCall(request).execute()
            val inputStream = response.body()?.byteStream() ?: return@withContext feed
            val articles = parser.parseArticlesFromRss(inputStream)
            val storedUrlList = articleRepository.getStoredUrlListIn(articles)
            val newArticleList = articles.filter { it.url !in storedUrlList }
                .map { it.copy(feedId = feed.id) }
            if (newArticleList.isEmpty()) {
                val size = articleRepository.getUnreadArticleCount(feed.id)
                rssRepository.updateUnreadArticleCount(feed.id, size)
                feed.unreadAriticlesCount = size
                Timber.d("finish update rss ${feed.title}, no update")
                return@withContext feed
            }

            val savedArtices = articleRepository.saveNewArticles(newArticleList)
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

    private inline fun <T> measureTimeMillsWithResult(block: () -> T): Pair<Long, T> {
        val now = System.currentTimeMillis()
        val result = block()
        val time = System.currentTimeMillis() - now
        return time to result
    }
}
