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
	private ArrayList<UpdateFeedThread> updateFeedThreads = new ArrayList<UpdateFeedThread>();
	private Context context;
	private RequestQueue mQueue;
	
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
	
	public boolean updateAllFeeds(ArrayList<Feed> feeds) {
		if(isUpdating()) {
			return false;
		}
		updateFeedThreads.clear();
		for(final Feed feed : feeds) {
			updateFeed(feed);
		}
		AlarmManagerTaskManager.setNewHatenaUpdateAlarm(context);
		return true;
	}
	
	public void updateFeed(final Feed feed) {
//		UpdateFeedThread thread = new UpdateFeedThread(context, feed);
//		updateFeedThreads.add(thread);
//		thread.start();
		InputStreamRequest request = new InputStreamRequest(feed.getUrl(),   
			       new Listener<InputStream>() {  
			  
			        @Override  
			        public void onResponse(InputStream in) {  
			        	if (in == null) {
			        		return;
			        	}
			            RssParser parser = new RssParser(context); 
			            try {
							parser.parseXml(in, feed.getId());
							in.close();
							context.sendBroadcast(new Intent(FeedListActivity.UPDATE_NUM_OF_ARTICLES));
						} catch (IOException e) {
							e.printStackTrace();
						}
			        }  
			    }, new ErrorListener() {  
			  
			        @Override  
			        public void onErrorResponse(VolleyError error) {  
			            // error  
			        }  
			    });  
			  
		mQueue.add(request);
	}
	
	public boolean isUpdating() {
		if(updateFeedThreads == null || updateFeedThreads.size() == 0) {
			return false;
		}
		for(UpdateFeedThread thread : updateFeedThreads) {
			if(thread.isRunning()) {
				return true;
			}
		}
		return false;
	}
}
