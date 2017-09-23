package com.phicdy.mycuration.task;

import android.support.annotation.NonNull;
import android.util.Log;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.util.TextUtil;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class GetHatenaBookmark {

    private static final String LOG_TAG = GetHatenaBookmark.class.toString();
    private ScheduledExecutorService executorService;
    private final DatabaseAdapter adapter;
    private final HatenaRequestService hatenaRequestService;

    private interface HatenaRequestService {
        @GET("entry.count/")
        Call<ResponseBody> bookmark(@Query("url") String url);
    }

    /**
     * Constructor
     *
     * @param adapter Database adapter to save the result
     */
    public GetHatenaBookmark(@NonNull DatabaseAdapter adapter) {
        this.adapter = adapter;
        executorService = Executors.newScheduledThreadPool(8);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.b.st-hatena.com")
                .build();
        hatenaRequestService = retrofit.create(HatenaRequestService.class);
    }

    /**
     *
     * Request Hatena bookmark API and save the result
     *
     * @param url URL to get Hatena bookmark
     */
    public void request(@NonNull final String url, int delaySec) {
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                Call<ResponseBody> call = hatenaRequestService.bookmark(url);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        String point = "0";
                        try {
                            ResponseBody body = response.body();
                            if (body == null) {
                                point = "0";
                            } else {
                                point = body.string();
                            }
                            if (TextUtil.isEmpty(point)) point = "0";
                            adapter.saveHatenaPoint(url, point);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d(LOG_TAG, "Hatena request done, " + url + ": " + point);
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                        Log.d(LOG_TAG, "Request error:" + throwable.toString());
                    }
                });
            }
        }, delaySec, TimeUnit.SECONDS);
    }
}
