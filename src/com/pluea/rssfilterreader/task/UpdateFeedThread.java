package com.pluea.rssfilterreader.task;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;

import com.pleua.rssfilterreader.rss.Article;
import com.pleua.rssfilterreader.rss.Feed;
import com.pleua.rssfilterreader.rss.RssParser;
import com.pluea.rssfilterreader.db.DatabaseAdapter;
import com.pluea.rssfilterreader.filter.FilterTask;
import com.pluea.rssfilterreader.ui.FeedListActivity;

public class UpdateFeedThread extends Thread {

	private Context context;
	private Feed feed;
	private boolean isRunning = false;
	
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

		DatabaseAdapter dbAdapter = new DatabaseAdapter(context);
		
		// Parse XML
		RssParser rssParser = new RssParser(context);
		boolean parseResult;
		try {
			parseResult = rssParser.parseXml(urlString,
					feedId);
			// Update articles "toRead" status to "read"
			if (!dbAdapter.changeArticlesStatusToRead()) {
				return;
			}
			
			// Filter articles
			if (parseResult) {
				new FilterTask(context).applyFiltering(feedId);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Update Hatena point
		ArrayList<Article> articles = dbAdapter
				.getUnreadArticlesInAFeed(feedId);
		for (int i = 0; i < articles.size(); i++) {
			Article article = articles.get(i);
			article.setArrayIndex(i);
			GetHatenaBookmarkPointTask hatenaTask = new GetHatenaBookmarkPointTask(
					context);
			hatenaTask.execute(article);
		}
		
		isRunning = false;
		
		// Broadcast updating num of articles
		context.sendBroadcast(new Intent(FeedListActivity.UPDATE_NUM_OF_ARTICLES));
	}

	public boolean isRunning() {
		return isRunning;
	}

}
