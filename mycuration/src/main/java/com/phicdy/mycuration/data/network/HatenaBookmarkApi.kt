package com.phicdy.mycuration.data.network


import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import timber.log.Timber

class HatenaBookmarkApi {

    private val hatenaRequestService: HatenaRequestService

    private interface HatenaRequestService {
        @GET("entry.count")
        fun bookmark(@Query("url") url: String): Deferred<Response<ResponseBody>>
    }

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl("http://api.b.st-hatena.com")
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
        hatenaRequestService = retrofit.create(HatenaRequestService::class.java)
    }

    /**
     *
     * Request Hatena bookmark API and save the result
     *
     * @param url URL to get Hatena bookmark
     */
    suspend fun request(url: String): String = withContext(Dispatchers.IO) {
        try {
            val response = hatenaRequestService.bookmark(url).await()
            return@withContext response.body()?.string() ?: "0"
        } catch (t: Throwable) {
            Timber.d("Request error:%s", t.toString())
        }
        return@withContext "0"
    }
}
