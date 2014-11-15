package com.pleua.rssfilterreader.rss.test;

import java.io.IOException;

import android.test.AndroidTestCase;
import android.test.suitebuilder.TestSuiteBuilder.FailedToCreateTests;

import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.rss.Feed;
import com.pluea.filfeed.rss.RssParser;
import com.pluea.filfeed.task.InsertNewFeedTask;

public class RssParserTest extends AndroidTestCase {

	public RssParserTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testParseFeedInfo() {
		// Delete test feed
		DatabaseAdapter adapter = new DatabaseAdapter(getContext());
		Feed feed = adapter.getFeedByUrl("http://jp.techcrunch.com/feed/");
		if(feed != null) {
			adapter.deleteFeed(feed.getId());
		}
		
		RssParser parser = new RssParser(getContext());
//		try {
//			Feed addedFeed = parser.parseFeedInfo("http://jp.techcrunch.com/feed/");
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//			assertNotNull(addedFeed);
//			assertEquals("http://jp.techcrunch.com/feed/", addedFeed.getUrl());
//			assertEquals("http://jp.techcrunch.com", addedFeed.getSiteUrl());
//			assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed.getIconPath());
//		} catch (IOException e) {
//			e.printStackTrace();
//			fail();
//		}
		
//		String publicKeyFeedUrl = "http://www.publickey1.jp/atom.xml";
//		Feed publicKeyFeed = adapter.getFeedByUrl(publicKeyFeedUrl);
//		if(publicKeyFeed != null) {
//			adapter.deleteFeed(publicKeyFeed.getId());
//		}
//		
//		try {
//			publicKeyFeed = parser.parseFeedInfo(publicKeyFeedUrl);
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//			assertNotNull(publicKeyFeed);
//			assertEquals("Publickey", publicKeyFeed.getTitle());
//			assertEquals(publicKeyFeedUrl, publicKeyFeed.getUrl());
//			assertEquals("http://www.publickey1.jp/", publicKeyFeed.getSiteUrl());
//			assertEquals(Feed.DEDAULT_ICON_PATH, publicKeyFeed.getIconPath());
//		} catch (IOException e) {
//			e.printStackTrace();
//			fail();
//		}
	}

	public void testParseXml() {
		RssParser parser = new RssParser(getContext());
		DatabaseAdapter adapter = new DatabaseAdapter(getContext());
		
		String publicKeyUrl = "http://www.publickey1.jp/atom.xml";
		Feed feed = adapter.getFeedByUrl(publicKeyUrl);
		try {
			boolean result = parser.parseXml(publicKeyUrl, feed.getId());
			assertEquals(true, result);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
}
