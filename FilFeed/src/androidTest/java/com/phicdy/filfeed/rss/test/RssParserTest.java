package com.phicdy.filfeed.rss.test;

import android.test.AndroidTestCase;

import com.phicdy.filfeed.db.DatabaseAdapter;
import com.phicdy.filfeed.rss.Feed;
import com.phicdy.filfeed.rss.RssParser;
import com.phicdy.filfeed.task.NetworkTaskManager;

public class RssParserTest extends AndroidTestCase {

	public RssParserTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		DatabaseAdapter adapter = DatabaseAdapter.getInstance(getContext());
		adapter.deleteAllArticles();
		adapter.deleteAllFeeds();
	}
	
	public void testParseFeedInfo() {
		// Delete test feed
		DatabaseAdapter adapter = DatabaseAdapter.getInstance(getContext());
		Feed feed = adapter.getFeedByUrl("http://jp.techcrunch.com/feed/");
		if(feed != null) {
			adapter.deleteFeed(feed.getId());
		}
		
		RssParser parser = new RssParser(getContext());
		NetworkTaskManager.getInstance(getContext()).addNewFeed("http://jp.techcrunch.com/feed/");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Feed addedFeed = adapter.getFeedByUrl("http://jp.techcrunch.com/feed/");
		assertNotNull(addedFeed);
		assertEquals("http://jp.techcrunch.com/feed/", addedFeed.getUrl());
		assertEquals("http://jp.techcrunch.com", addedFeed.getSiteUrl());
		assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed.getIconPath());
		adapter.deleteAllFeeds();
		
		/*String publicKeyFeedUrl = "http://www.publickey1.jp/atom.xml";
		Feed publicKeyFeed = adapter.getFeedByUrl(publicKeyFeedUrl);
		if(publicKeyFeed != null) {
			adapter.deleteFeed(publicKeyFeed.getId());
		}

		try {
			publicKeyFeed = parser.parseFeedInfo(publicKeyFeedUrl);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			assertNotNull(publicKeyFeed);
			assertEquals("Publickey", publicKeyFeed.getTitle());
			assertEquals(publicKeyFeedUrl, publicKeyFeed.getUrl());
			assertEquals("http://www.publickey1.jp/", publicKeyFeed.getSiteUrl());
			assertEquals(Feed.DEDAULT_ICON_PATH, publicKeyFeed.getIconPath());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}*/
		
		// Test top URL
		NetworkTaskManager.getInstance(getContext()).addNewFeed("http://jp.techcrunch.com");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		addedFeed = adapter.getFeedByUrl("http://jp.techcrunch.com/feed/");
		assertNotNull(addedFeed);
		assertEquals("http://jp.techcrunch.com/feed/", addedFeed.getUrl());
		assertEquals("http://jp.techcrunch.com", addedFeed.getSiteUrl());
		assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed.getIconPath());

//		try {
//		NetworkTaskManager.getInstance(getContext()).addNewFeed("http://ground-sesame.hatenablog.jp");
////			Feed surigomaFeed = parser.parseFeedInfo("http://ground-sesame.hatenablog.jp");
//		try {
//			Thread.sleep(7000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//
//		Feed surigomaFeed = adapter.getFeedByUrl("http://ground-sesame.hatenablog.jp");
//		assertNotNull(surigomaFeed);
//			assertEquals("http://ground-sesame.hatenablog.jp/", surigomaFeed.getUrl());
//			assertEquals("http://ground-sesame.hatenablog.jp/feed", surigomaFeed.getSiteUrl());
//			assertEquals(surigomaFeed.DEDAULT_ICON_PATH, surigomaFeed.getIconPath());
//		} catch (IOException e) {
//			e.printStackTrace();
//			fail();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

}
