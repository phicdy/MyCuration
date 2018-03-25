package com.phicdy.mycuration.domain.task

import android.support.annotation.IntDef
import android.util.Log
import com.phicdy.mycuration.data.filter.FilterTask
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.domain.rss.UnreadCountManager
import com.phicdy.mycuration.util.FileUtil
import com.phicdy.mycuration.util.TextUtil
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url
import java.io.IOException
import java.io.InputStream
import java.net.URI

object NetworkTaskManager {

    const val FINISH_UPDATE_ACTION = "FINISH_UPDATE"
    const val ERROR_INVALID_URL = 1L
    const val ERROR_NON_RSS_HTML_CONTENT = 2L
    const val ERROR_UNKNOWN = 3L
    const val REASON_NOT_FOUND = -1

    val isUpdatingFeed: Boolean get() = false

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(ERROR_INVALID_URL, ERROR_NON_RSS_HTML_CONTENT, ERROR_UNKNOWN)
    annotation class AddFeedUrlError

    fun updateAllFeeds(feeds: ArrayList<Feed>): Flowable<Feed> {
        return Flowable.fromIterable(feeds)
                .subscribeOn(Schedulers.io())
                .filter { it.id > 0 }
                .flatMap {
                    Log.d("test", "flatMap, Thread:" + Thread.currentThread().name + ", feed:" + it.title)
                    return@flatMap Flowable.just(it).subscribeOn(Schedulers.newThread())
                }
                .doOnNext({
                    Log.d("doOnNext", "Thread:" + Thread.currentThread().name + ", it:" + it.title)
                    updateFeed(it) ?: return@doOnNext
                })
    }

    private interface FeedRequestService {
        @GET
        fun feeds(@Url url: String): Call<ResponseBody>
    }

    fun updateFeed(feed: Feed): InputStream? {
        if (TextUtil.isEmpty(feed.url)) return null
        val uri = URI.create(feed.url)
        val retrofit = Retrofit.Builder()
                .baseUrl(uri.scheme + "://" + uri.host)
                .build()
        val service = retrofit.create(FeedRequestService::class.java)
        val call = service.feeds(uri.path)
        try {
            val response = call.execute()
            if (response.body() == null) {
                return null
            }
            val inputStream = response.body().byteStream()
            val parser = RssParser()
            parser.parseXml(inputStream, feed.id)
            FilterTask().applyFiltering(feed.id)
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (feed.iconPath == null || feed.iconPath == Feed.DEDAULT_ICON_PATH) {
                val iconSaveFolderStr = FileUtil.iconSaveFolder()
                val task = GetFeedIconTask(iconSaveFolderStr)
                task.execute(feed.siteUrl)
            }
            UnreadCountManager.getInstance().refreshConut(feed.id)
        } catch (e: IOException) {

        } catch (e: RuntimeException) {

        }
        return null
    }
}
