package com.phicdy.mycuration.task;

import android.support.annotation.NonNull;
import android.util.Log;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.util.TextUtil;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class GetHatenaBookmark {

    private static final String LOG_TAG = GetHatenaBookmark.class.toString();

    private interface HatenaRequestService {
        @GET("entry.count/")
        Call<ResponseBody> bookmark(@Query("url") String url);
    }

    /**
     *
     * Request Hatena bookmark API and save the result
     *
     * @param url URL to get Hatena bookmark
     * @param adapter Database adapter to save the result
     */
    public void request(@NonNull final String url, @NonNull final DatabaseAdapter adapter) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.b.st-hatena.com")
                .build();
        HatenaRequestService hatenaRequestService = retrofit.create(HatenaRequestService.class);
        Call<ResponseBody> call = hatenaRequestService.bookmark(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String point = response.body().string();
                    if (TextUtil.isEmpty(point)) point = "0";
                    adapter.saveHatenaPoint(url, point);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                Log.d(LOG_TAG, "Request error:" + throwable.toString());
            }
        });
    }
}
