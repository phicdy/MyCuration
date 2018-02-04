package com.phicdy.mycuration.task;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;

import com.phicdy.mycuration.filter.FilterTask;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.rss.RssParser;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.phicdy.mycuration.util.TextUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Url;

public class NetworkTaskManager {

	private static NetworkTaskManager networkTaskManager;
	private static ExecutorService executorService;
	private final Context context;
	// Manage queue status
	private int numOfFeedRequest = 0;

	public static final String FINISH_ADD_FEED = "FINISH_ADD_FEED";
    public static final String FINISH_UPDATE_ACTION = "FINISH_UPDATE";
	public static final String ADDED_FEED_URL = "ADDED_FEED_URL";
	public static final String ADD_FEED_ERROR_REASON = "ADDED_FEED_ERROR_REASON";
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ERROR_INVALID_URL, ERROR_NON_RSS_HTML_CONTENT, ERROR_UNKNOWN})
	public @interface AddFeedUrlError {}
	public static final int ERROR_INVALID_URL = 1;
	public static final int ERROR_NON_RSS_HTML_CONTENT = 2;
	public static final int ERROR_UNKNOWN = 3;
	public static final int REASON_NOT_FOUND = -1;

	private NetworkTaskManager(Context context) {
		this.context = context;
		executorService = Executors.newFixedThreadPool(8);
	}

	public static NetworkTaskManager getInstance(Context context) {
		if (networkTaskManager == null) {
			networkTaskManager = new NetworkTaskManager(context);
		}
		return networkTaskManager;
	}

	public void updateAllFeeds(final ArrayList<Feed> feeds) {
		if (isUpdatingFeed()) {
			return;
		}
		numOfFeedRequest = 0;
		for (final Feed feed : feeds) {
			updateFeed(feed);
			if (feed.getIconPath() == null || feed.getIconPath().equals(Feed.DEDAULT_ICON_PATH)) {
				GetFeedIconTask task = new GetFeedIconTask(context);
				task.execute(feed.getSiteUrl());
			}
		}
	}

	private interface FeedRequestService {
        @GET
        Call<ResponseBody> feeds(@Url String url);
    }

	public void updateFeed(final Feed feed) {
        if (feed == null || TextUtil.INSTANCE.isEmpty(feed.getUrl())) return;
        URI uri = URI.create(feed.getUrl());
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(uri.getScheme() + "://" + uri.getHost())
                .build();
        FeedRequestService service = retrofit.create(FeedRequestService.class);
        Call<ResponseBody> call = service.feeds(uri.getPath());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null || response.body() == null) {
                    finishOneRequest(feed.getId());
					return;
				}
                UpdateFeedTask task = new UpdateFeedTask(response.body().byteStream(), feed.getId());
                executorService.execute(task);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
				finishOneRequest(feed.getId());
            }
        });

		addNumOfRequest();
	}

	private synchronized void addNumOfRequest() {
		numOfFeedRequest++;
	}
	
	private synchronized void finishOneRequest(int feedId) {
		numOfFeedRequest--;
		UnreadCountManager.getInstance(context).refreshConut(feedId);
		context.sendBroadcast(new Intent(FINISH_UPDATE_ACTION));
	}
	
	public synchronized boolean isUpdatingFeed() {
		return numOfFeedRequest != 0;
	}
	
	private class UpdateFeedTask implements Runnable {

		private final InputStream in;
		private final int feedId;

		UpdateFeedTask(InputStream in, int feedId) {
			this.in = in;
			this.feedId = feedId;
		}

		@Override
		public void run() {
			RssParser parser = new RssParser(context);
			parser.parseXml(in, feedId);
			new FilterTask(context).applyFiltering(feedId);
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			finishOneRequest(feedId);
		}
	}
}
