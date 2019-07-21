package com.phicdy.mycuration.domain.task

import com.phicdy.mycuration.data.network.HatenaBookmarkApi
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.entity.Feed
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url
import timber.log.Timber
import java.io.IOException
import java.net.URI

class NetworkTaskManager(private val articleRepository: ArticleRepository,
                         private val rssRepository: RssRepository,
                         private val curationRepository: CurationRepository,
                         private val filterRepository: FilterRepository) {

    val isUpdatingFeed: Boolean get() = false

    fun updateAllFeeds(feeds: ArrayList<Feed>): Flowable<Feed> {
        return Flowable.fromIterable(feeds)
                .subscribeOn(Schedulers.io())
                .filter { feed -> feed.id > 0 }
                .flatMap({ data -> Flowable.just(data).subscribeOn(Schedulers.io()) })
                { _, newData ->
                    runBlocking {
                        updateFeed(newData)
                    }
                    newData
                }
    }

    suspend fun updateAll(rssList: List<Feed>) = withContext(Dispatchers.IO) {
        rssList.filter { it.id > 0 }
                .map { async { updateFeed(it) } }
                .map { it.await() }
    }

    private interface FeedRequestService {
        @GET
        fun feeds(@Url url: String): Call<ResponseBody>
    }

    suspend fun updateFeed(feed: Feed) = coroutineScope {
        if (feed.url.isEmpty()) return@coroutineScope
        val uri = URI.create(feed.url)
        val retrofit = Retrofit.Builder()
                .baseUrl(uri.scheme + "://" + uri.host)
                .build()
        val service = retrofit.create(FeedRequestService::class.java)
        val path = uri.toString().substring((uri.scheme + "://" + uri.host).length)
        val call = service.feeds(path)
        try {
            val response = withContext(Dispatchers.IO) { call.execute() }
            if (response.body() == null) {
                return@coroutineScope
            }
            val inputStream = response.body()?.byteStream() ?: return@coroutineScope
            val parser = RssParser()
            val articles = parser.parseArticlesFromRss(inputStream)
            articles.filter { !articleRepository.isExistArticleOf(url = it.url) }
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
