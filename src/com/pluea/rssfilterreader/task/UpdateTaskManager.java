package com.pluea.rssfilterreader.task;

import java.util.ArrayList;

import android.content.Context;

import com.pleua.rssfilterreader.rss.Feed;


public class UpdateTaskManager {

	private static UpdateTaskManager updateTaskManager;
	private ArrayList<UpdateFeedThread> updateFeedThreads = new ArrayList<UpdateFeedThread>();
	private Context context;
	
	private UpdateTaskManager(Context context) {
		this.context = context;
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
		for(Feed feed : feeds) {
			UpdateFeedThread thread = new UpdateFeedThread(context, feed);
			updateFeedThreads.add(thread);
			thread.start();
		}
		return true;
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
