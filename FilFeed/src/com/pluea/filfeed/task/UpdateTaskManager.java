package com.pluea.filfeed.task;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.pluea.filfeed.alarm.AlarmManagerTaskManager;
import com.pluea.filfeed.filter.FilterTask;
import com.pluea.filfeed.rss.Feed;
import com.pluea.filfeed.rss.RssParser;
import com.pluea.filfeed.ui.FeedListActivity;

public class UpdateTaskManager {

	private static UpdateTaskManager updateTaskManager;
	private Context context;
	private RequestQueue mQueue;
	// Manage queue status
	private int numOfFeedRequest = 0;
	
	private static final String LOG_TAG = "RSSReader.UpdateTaskManager";
	
	private UpdateTaskManager(Context context) {
		this.context = context;
		mQueue = Volley.newRequestQueue(context);
	}
	
	public static UpdateTaskManager getInstance(Context context) {
		if(updateTaskManager == null) {
			updateTaskManager = new UpdateTaskManager(context);
		}
		return updateTaskManager;
	}
	
	public boolean updateAllFeeds(final ArrayList<Feed> feeds) {
		if(isUpdatingFeed()) {
			return false;
		}
		numOfFeedRequest = 0;
		for(final Feed feed : feeds) {
			updateFeed(feed);
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
			        	Log.d(LOG_TAG, "Request error:" + error.getMessage());
			        	finishOneRequest();
			        	context.sendBroadcast(new Intent(FeedListActivity.FINISH_UPDATE_ACTION));
			        }  
			    });  
			  
		addNumOfRequest();
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
}
