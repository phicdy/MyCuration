package com.phicdy.mycuration.domain.task

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.network.HatenaBookmarkApi
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.domain.filter.FilterTask
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.util.TextUtil
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url
import java.io.IOException
import java.net.URI

class NetworkTaskManager(private val articleRepository: ArticleRepository,
                         private val rssRepository: RssRepository,
                         private val curationRepository: CurationRepository,
                         private val unreadCountRepository: UnreadCountRepository) {

    val isUpdatingFeed: Boolean get() = false

    fun updateAllFeeds(feeds: ArrayList<Feed>): Flowable<Feed> {
        return Flowable.fromIterable<Feed>(feeds)
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

    private interface FeedRequestService {
        @GET
        fun feeds(@Url url: String): Call<ResponseBody>
    }

    suspend fun updateFeed(feed: Feed) = coroutineScope {
        if (TextUtil.isEmpty(feed.url)) return@coroutineScope
        val uri = URI.create(feed.url)
        val retrofit = Retrofit.Builder()
                .baseUrl(uri.scheme + "://" + uri.host)
                .build()
        val service = retrofit.create(FeedRequestService::class.java)
        val call = service.feeds(uri.path)
        try {
            val response = call.execute()
            if (response.body() == null) {
                return@coroutineScope
            }
            val inputStream = response.body()?.byteStream() ?: return@coroutineScope
            val parser = RssParser()
            val dbAdapter = DatabaseAdapter.getInstance()
            val latestDate = dbAdapter.getLatestArticleDate(feed.id)
            val articles = parser.parseXml(inputStream, latestDate)

            if (articles.size > 0) {
                val savedArtices = articleRepository.saveNewArticles(articles, feed.id)
                curationRepository.saveCurationsOf(savedArtices)
                val hatenaBookmarkApi = HatenaBookmarkApi(dbAdapter)
                var delaySec = 0
                for ((i, article) in articles.withIndex()) {
                    hatenaBookmarkApi.request(article.url, delaySec)
                    if (i % 10 == 0) delaySec += 2
                }
                unreadCountRepository.appendUnreadArticleCount(feed.id, articles.size)
            }

            val updatedCount = FilterTask(articleRepository).applyFiltering(feed.id)
            unreadCountRepository.decreaseCount(feed.id, updatedCount)
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

        } catch (e: RuntimeException) {

        }
    }
}
