package com.pluea.filfeed.task;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.filter.FilterTask;
import com.pluea.filfeed.rss.Article;
import com.pluea.filfeed.rss.Feed;
import com.pluea.filfeed.rss.RssParser;
import com.pluea.filfeed.ui.FeedListActivity;

public class UpdateFeedThread extends Thread {

	private Context context;
	private Feed feed;
	private boolean isRunning = false;
	
	private static final String LOG_TAG = "RSSREADER."
			+ UpdateFeedThread.class.getSimpleName();
	
	public UpdateFeedThread(Context context, Feed feed) {
		this.context = context;
		this.feed = feed;
	}
	
	@Override
	public void run() {
		isRunning = true;
		
		// Set URL string and id
		String urlString = feed.getUrl();
		int feedId = feed.getId();

		DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(context);
		
		// Parse XML
		RssParser rssParser = new RssParser(context);
		boolean parseResult;
		try {
			parseResult = rssParser.parseXml(urlString,
					feedId);
			// Update articles "toRead" status to "read"
			dbAdapter.changeArticlesStatusToRead();
			
			// Filter articles
			if (parseResult) {
				new FilterTask(context).applyFiltering(feedId);
			}
		} catch (IOException e) {
			Log.d(LOG_TAG, "Parse error");
			e.printStackTrace();
		}

		isRunning = false;
		
		// Broadcast updating num of articles
		context.sendBroadcast(new Intent(FeedListActivity.UPDATE_NUM_OF_ARTICLES));
	}

	public boolean isRunning() {
		return isRunning;
	}

}
