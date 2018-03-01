package com.phicdy.mycuration.task

import android.content.Context
import android.content.Intent
import android.support.annotation.IntDef

import com.phicdy.mycuration.filter.FilterTask
import com.phicdy.mycuration.rss.Feed
import com.phicdy.mycuration.rss.RssParser
import com.phicdy.mycuration.rss.UnreadCountManager
import com.phicdy.mycuration.util.FileUtil
import com.phicdy.mycuration.util.TextUtil

import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.util.ArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url

object NetworkTaskManager {
    // Manage queue status
    private var numOfFeedRequest = 0
    private var executorService: ExecutorService
    private lateinit var context: Context

    const val FINISH_ADD_FEED = "FINISH_ADD_FEED"
    const val FINISH_UPDATE_ACTION = "FINISH_UPDATE"
    const val ADDED_FEED_URL = "ADDED_FEED_URL"
    const val ADD_FEED_ERROR_REASON = "ADDED_FEED_ERROR_REASON"
    const val ERROR_INVALID_URL = 1L
    const val ERROR_NON_RSS_HTML_CONTENT = 2L
    const val ERROR_UNKNOWN = 3L
    const val REASON_NOT_FOUND = -1

    val isUpdatingFeed: Boolean
        @Synchronized get() = numOfFeedRequest != 0

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(ERROR_INVALID_URL, ERROR_NON_RSS_HTML_CONTENT, ERROR_UNKNOWN)
    annotation class AddFeedUrlError

    init {
        executorService = Executors.newFixedThreadPool(8)
    }

    fun setUp(context: Context) {
        this.context = context
    }

    fun updateAllFeeds(feeds: ArrayList<Feed>) {
        if (isUpdatingFeed) {
            return
        }
        numOfFeedRequest = 0
        for (feed in feeds) {
            updateFeed(feed)
            if (feed.iconPath == null || feed.iconPath == Feed.DEDAULT_ICON_PATH) {
                val iconSaveFolderStr = FileUtil.iconSaveFolder(context)
                val task = GetFeedIconTask(iconSaveFolderStr)
                task.execute(feed.siteUrl)
            }
        }
    }

    private interface FeedRequestService {
        @GET
        fun feeds(@Url url: String): Call<ResponseBody>
    }

    fun updateFeed(feed: Feed?) {
        if (feed == null || TextUtil.isEmpty(feed.url)) return
        val uri = URI.create(feed.url)
        val retrofit = Retrofit.Builder()
                .baseUrl(uri.scheme + "://" + uri.host)
                .build()
        val service = retrofit.create(FeedRequestService::class.java)
        val call = service.feeds(uri.path)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>?) {
                if (response?.body() == null) {
                    finishOneRequest(feed.id)
                    return
                }
                val task = UpdateFeedTask(response.body().byteStream(), feed.id)
                executorService.execute(task)
            }

            override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
                finishOneRequest(feed.id)
            }
        })

        addNumOfRequest()
    }

    @Synchronized
    private fun addNumOfRequest() {
        numOfFeedRequest++
    }

    @Synchronized
    private fun finishOneRequest(feedId: Int) {
        numOfFeedRequest--
        UnreadCountManager.getInstance().refreshConut(feedId)
        context.sendBroadcast(Intent(FINISH_UPDATE_ACTION))
    }

    private class UpdateFeedTask internal constructor(private val `in`: InputStream, private val feedId: Int) : Runnable {

        override fun run() {
            val parser = RssParser()
            parser.parseXml(`in`, feedId)
            FilterTask().applyFiltering(feedId)
            try {
                `in`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            finishOneRequest(feedId)
        }
    }

}
