package com.phicdy.mycuration.task;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.phicdy.mycuration.filter.FilterTask;
import com.phicdy.mycuration.rss.Article;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.rss.RssParser;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.phicdy.mycuration.view.fragment.FeedListFragment;
import com.phicdy.mycuration.util.UrlUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkTaskManager {

	private static NetworkTaskManager networkTaskManager;
	private static ExecutorService executorService;
	private final Context context;
	private final RequestQueue mQueue;
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
		mQueue = Volley.newRequestQueue(context);
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

	public void updateFeed(final Feed feed) {
		InputStreamRequest request = new InputStreamRequest(feed.getUrl(),
				new Listener<InputStream>() {

					@Override
					public void onResponse(final InputStream in) {
						if (in == null) {
							return;
						}
						executorService.execute(new UpdateFeedTask(in, feed.getId()));
					}
				}, new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        finishOneRequest();
                        UnreadCountManager.getInstance(context).refreshConut(feed.getId());
                        context.sendBroadcast(new Intent(FINISH_UPDATE_ACTION));
                    }
				});

		addNumOfRequest();
		mQueue.add(request);
	}

	public void addNewFeed(String feedUrl) {
		final String requestUrl = UrlUtil.removeUrlParameter(feedUrl);
        RssParser parser = new RssParser(context);
		parser.parseRssXml(requestUrl);
	}


	private synchronized void addNumOfRequest() {
		numOfFeedRequest++;
	}
	
	private synchronized void finishOneRequest() {
		numOfFeedRequest--;
	}
	
	public synchronized boolean isUpdatingFeed() {
		return numOfFeedRequest != 0;
	}
	
	void addHatenaBookmarkUpdateRequest(InputStreamRequest request) {
		if (request != null) {
			mQueue.add(request);
		}
	}

    public synchronized int getFeedRequestCountInQueue() {
        return numOfFeedRequest;
    }

	public void getHatenaPoint(Article article) {
		executorService.execute(new GetHatenaPointTask(article));
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
			finishOneRequest();
			UnreadCountManager.getInstance(context).refreshConut(feedId);
			context.sendBroadcast(new Intent(FINISH_UPDATE_ACTION));
		}
	}

	private class GetHatenaPointTask implements Runnable {

		private Article article;

		GetHatenaPointTask(Article article) {
			this.article = article;
		}

		@Override
		public void run() {
			GetHatenaBookmarkPointTask hatenaTask = new GetHatenaBookmarkPointTask(context);
			hatenaTask.execute(article);
		}
	}
}
