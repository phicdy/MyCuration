package com.phicdy.filfeed.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.phicdy.filfeed.alarm.AlarmManagerTaskManager;
import com.phicdy.filfeed.db.DatabaseAdapter;
import com.phicdy.filfeed.filter.FilterTask;
import com.phicdy.filfeed.rss.Feed;
import com.phicdy.filfeed.rss.RssParser;
import com.phicdy.filfeed.ui.TopActivity;
import com.phicdy.filfeed.util.UrlUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkTaskManager {

	private static NetworkTaskManager networkTaskManager;
	private static ExecutorService executorService;
	private Context context;
	private RequestQueue mQueue;
	// Manage queue status
	private int numOfFeedRequest = 0;

	public static final String FINISH_ADD_FEED = "FINISH_ADD_FEED";
	public static final String ADDED_FEED_URL = "ADDED_FEED_URL";
	private static final String LOG_TAG = "RSSReader.NetworkTaskManager";

	private NetworkTaskManager(Context context) {
		this.context = context;
		mQueue = Volley.newRequestQueue(context);
	}

	public static NetworkTaskManager getInstance(Context context) {
		if (networkTaskManager == null) {
			networkTaskManager = new NetworkTaskManager(context);
		}
		executorService = Executors.newFixedThreadPool(5);
		return networkTaskManager;
	}

	public boolean updateAllFeeds(final ArrayList<Feed> feeds) {
		if (isUpdatingFeed()) {
			return false;
		}
		numOfFeedRequest = 0;
		for (final Feed feed : feeds) {
			updateFeed(feed);
			if (feed.getIconPath() == null || feed.getIconPath().equals(Feed.DEDAULT_ICON_PATH)) {
				GetFeedIconTask task = new GetFeedIconTask(context);
				task.execute(feed.getSiteUrl());
			}
		}
		// After update feed, update hatena point with interval
		AlarmManagerTaskManager.setNewHatenaUpdateAlarmAfterFeedUpdate(context);
		return true;
	}

	public void updateFeed(final Feed feed) {
		InputStreamRequest request = new InputStreamRequest(feed.getUrl(),
				new Listener<InputStream>() {

					@Override
					public void onResponse(final InputStream in) {
						if (in == null) {
							return;
						}
						new Thread(new Runnable() {

							@Override
							public void run() {
								RssParser parser = new RssParser(context);
								try {
									parser.parseXml(in, feed.getId());
									new FilterTask(context).applyFiltering(feed.getId());
								} catch (IOException e) {
									e.printStackTrace();
								} finally {
									try {
										in.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
									finishOneRequest();
									context.sendBroadcast(new Intent(FeedListActivity.FINISH_UPDATE_ACTION));
								}
							}
						}).start();
					}
				}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				finishOneRequest();
				context.sendBroadcast(new Intent(TopActivity.FINISH_UPDATE_ACTION));
			}
		});

		addNumOfRequest();
		mQueue.add(request);
	}

	public void addNewFeed(String feedUrl) {
		final String requestUrl = UrlUtil.removeUrlParameter(feedUrl);
		InputStreamRequest request = new InputStreamRequest(requestUrl,
				new Listener<InputStream>() {

					@Override
					public void onResponse(final InputStream in) {
						RssParser parser = new RssParser(context);
						try {
							boolean isSucceeded = parser.parseFeedInfo(in, requestUrl);
							//Update new feed
							if(isSucceeded) {
								//Get Feed id from feed URL
								DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(context);
								Feed feed = dbAdapter.getFeedByUrl(requestUrl);

								//Parse XML and get new Articles
								if (feed != null) {
									NetworkTaskManager taskManager = NetworkTaskManager.getInstance(context);
									taskManager.updateFeed(feed);
									Intent intent = new Intent(FINISH_ADD_FEED);
									intent.putExtra(ADDED_FEED_URL, requestUrl);
									context.sendBroadcast(intent);
								}
							}else {
								Intent intent = new Intent(FINISH_ADD_FEED);
								context.sendBroadcast(intent);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
			}
		});

		mQueue.add(request);
	}

	private synchronized void addNumOfRequest() {
		numOfFeedRequest++;
	}
	
	private synchronized void finishOneRequest() {
		numOfFeedRequest--;
	}
	
	public synchronized boolean isUpdatingFeed() {
		if (numOfFeedRequest == 0) {
			return false;
		}
		return true;
	}
	
	public void addHatenaBookmarkUpdateRequest(InputStreamRequest request) {
		if (request != null) {
			mQueue.add(request);
		}
	}

    public synchronized int getFeedRequestCountInQueue() {
        return numOfFeedRequest;
    }
}
