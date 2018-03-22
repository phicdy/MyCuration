package com.phicdy.mycuration.domain.task

import android.util.Log

import com.phicdy.mycuration.db.DatabaseAdapter
import com.phicdy.mycuration.util.TextUtil

import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

class GetHatenaBookmark(private val adapter: DatabaseAdapter) {

    private val executorService: ScheduledExecutorService = Executors.newScheduledThreadPool(8)
    private val hatenaRequestService: HatenaRequestService

    companion object {
        private val LOG_TAG = GetHatenaBookmark::class.java.toString()
    }

    private interface HatenaRequestService {
        @GET("entry.count/")
        fun bookmark(@Query("url") url: String): Call<ResponseBody>
    }

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl("http://api.b.st-hatena.com")
                .build()
        hatenaRequestService = retrofit.create(HatenaRequestService::class.java)
    }

    /**
     *
     * Request Hatena bookmark API and save the result
     *
     * @param url URL to get Hatena bookmark
     */
    fun request(url: String, delaySec: Int) {
        executorService.schedule({
            val call = hatenaRequestService.bookmark(url)
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    var point = "0"
                    try {
                        val body = response.body()
                        point = if (body == null) {
                            "0"
                        } else {
                            body.string()
                        }
                        if (TextUtil.isEmpty(point)) point = "0"
                        adapter.saveHatenaPoint(url, point)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    Log.d(LOG_TAG, "Hatena request done, $url: $point")
                }

                override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
                    Log.d(LOG_TAG, "Request error:" + throwable.toString())
                }
            })
        }, delaySec.toLong(), TimeUnit.SECONDS)
    }
}
