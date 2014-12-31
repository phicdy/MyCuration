package com.pluea.filfeed.task;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.pluea.filfeed.alarm.AlarmManagerTaskManager;
import com.pluea.filfeed.rss.Feed;
import com.pluea.filfeed.rss.RssParser;
import com.pluea.filfeed.ui.FeedListActivity;

public class UpdateTaskManager {

	private static UpdateTaskManager updateTaskManager;
	private Context context;
	private RequestQueue mQueue;
	// Manage queue status
	private int numOfRequest = 0;
	
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
		if(isUpdating()) {
			return false;
		}
		numOfRequest = 0;
		for(final Feed feed : feeds) {
			updateFeed(feed);
		}
		// After update feed, update hatena point with interval
		AlarmManagerTaskManager.setNewHatenaUpdateAlarm(context);
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
							in.close();
			        	new Thread(new Runnable() {
			    			
			    			@Override
			    			public void run() {
					            RssParser parser = new RssParser(context); 
					            try {
									parser.parseXml(in, feed.getId());
								} catch (IOException e) {
									e.printStackTrace();
								} finally {
									finishOneRequest();
									context.sendBroadcast(new Intent(FeedListActivity.UPDATE_NUM_OF_ARTICLES));
								}
					    	}
			        	}).start();
			        }  
			    }, new ErrorListener() {  
			  
			        @Override  
			        public void onErrorResponse(VolleyError error) {  
			            // error  
			        }  
			    });  
			  
		addNumOfRequest();
		mQueue.add(request);
	}
	
	private synchronized void addNumOfRequest() {
		numOfRequest++;
	}
	
	private synchronized void finishOneRequest() {
		numOfRequest--;
	}
	
	public synchronized boolean isUpdating() {
		if (numOfRequest == 0) {
			return false;
		}
		return true;
	}
}
