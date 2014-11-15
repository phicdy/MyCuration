package com.pluea.rssfilterreader.task.test;

import android.test.AndroidTestCase;

import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.rss.Feed;
import com.pluea.filfeed.task.InsertNewFeedTask;

public class InsertNewFeedTaskTest extends AndroidTestCase {

	public InsertNewFeedTaskTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testDoInBackground() {
		// Delete test feed
		DatabaseAdapter adapter = new DatabaseAdapter(getContext());
		Feed feed = adapter.getFeedByUrl("http://jp.techcrunch.com/feed/");
		if(feed != null) {
			adapter.deleteFeed(feed.getId());
		}
		
		InsertNewFeedTask task = new InsertNewFeedTask(getContext());
		task.execute("http://jp.techcrunch.com/feed/");
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Feed techfeed = adapter.getFeedByUrl("http://jp.techcrunch.com/feed/");
		assertNotNull(techfeed);
		assertEquals("http://jp.techcrunch.com/feed/", techfeed.getUrl());
		assertEquals("http://jp.techcrunch.com", techfeed.getSiteUrl());
		assertEquals(Feed.DEDAULT_ICON_PATH, techfeed.getIconPath());
		
		Feed greeBlogFeed = adapter.getFeedByUrl("http://labs.gree.jp/blog/feed/");
		if(greeBlogFeed != null) {
			adapter.deleteFeed(feed.getId());
		}
		
		InsertNewFeedTask insertGreeBlogTask = new InsertNewFeedTask(getContext());
		insertGreeBlogTask.execute("http://labs.gree.jp/blog/feed/");
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		greeBlogFeed = adapter.getFeedByUrl("http://labs.gree.jp/blog/feed/");
		assertNotNull(greeBlogFeed);
		assertEquals("http://labs.gree.jp/blog/feed/", greeBlogFeed.getUrl());
		assertEquals("http://labs.gree.jp/blog", greeBlogFeed.getSiteUrl());
		assertEquals(Feed.DEDAULT_ICON_PATH, greeBlogFeed.getIconPath());
		
	}

}
