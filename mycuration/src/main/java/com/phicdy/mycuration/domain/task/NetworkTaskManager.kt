package com.phicdy.mycuration.domain.task

import android.util.Log
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.domain.filter.FilterTask
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.util.FileUtil
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
                         private val unreadCountRepository: UnreadCountRepository) {

    val isUpdatingFeed: Boolean get() = false

    fun updateAllFeeds(feeds: ArrayList<Feed>): Flowable<Feed> {
        return Flowable.fromIterable<Feed>(feeds)
                .subscribeOn(Schedulers.io())
                .filter { feed -> feed.id > 0 }
                .flatMap({ data -> Flowable.just(data).subscribeOn(Schedulers.io()) })
                { _, newData ->
                    Log.d("NetworkTask", "BiFunction, Thread:" + Thread.currentThread().name + ", feed:" + newData.title)
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
                dbAdapter.saveNewArticles(articles, feed.id)
                val getHatenaBookmark = GetHatenaBookmark(dbAdapter)
                var delaySec = 0
                for ((i, article) in articles.withIndex()) {
                    getHatenaBookmark.request(article.url, delaySec)
                    if (i % 10 == 0) delaySec += 2
                }
                unreadCountRepository.appendUnreadArticleCount(feed.id, articles.size)
            }

            FilterTask(articleRepository).applyFiltering(feed.id)
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (feed.iconPath == Feed.DEDAULT_ICON_PATH) {
                val iconSaveFolderStr = FileUtil.iconSaveFolder()
                val task = GetFeedIconTask(iconSaveFolderStr)
                task.execute(feed.siteUrl)
            }
        } catch (e: IOException) {

        } catch (e: RuntimeException) {

        }
    }
}
