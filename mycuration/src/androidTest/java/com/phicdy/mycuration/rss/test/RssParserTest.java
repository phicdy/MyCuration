package com.phicdy.mycuration.rss.test;

import android.support.test.runner.AndroidJUnit4;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.task.NetworkTaskManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getContext;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class RssParserTest {

	private DatabaseAdapter adapter;

	public RssParserTest() {
		super();
	}

	@Before
	public void setUp() throws Exception {
		adapter = DatabaseAdapter.getInstance(getTargetContext());
		adapter.deleteAllArticles();
		adapter.deleteAll();
	}

	@After
	public void tearDown() throws Exception {
		adapter.deleteAllArticles();
		adapter.deleteAll();
	}

	@Test
	public void testParseFeedInfoRSS1() {
		NetworkTaskManager.getInstance(getContext()).addNewFeed("http://news.yahoo.co.jp/pickup/rss.xml");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Feed addedFeed = adapter.getFeedByUrl("http://news.yahoo.co.jp/pickup/rss.xml");
//		ArrayList<Feed> feeds = adapter.getAllFeedsWithNumOfUnreadArticles();
		assertNotNull(addedFeed);
		assertEquals("http://news.yahoo.co.jp/pickup/rss.xml", addedFeed.getUrl());
		assertEquals("http://news.yahoo.co.jp", addedFeed.getSiteUrl());
		assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed.getIconPath());

		//http://feed.japan.cnet.com/rss/index.rdf
		//http://itpro.nikkeibp.co.jp/rss/ITpro.rdf
		//http://blog.livedoor.jp/itsoku/index.rdf
		//http://sierblog.com/index.rdf
	}

	@Test
	public void testParseFeedInfoRSS1_rdf() {
		String testUrl = "http://b.hatena.ne.jp/hotentry/it.rss";
		NetworkTaskManager.getInstance(getContext()).addNewFeed(testUrl);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Feed addedFeed = adapter.getFeedByUrl(testUrl);
		assertNotNull(addedFeed);
		assertEquals(testUrl, addedFeed.getUrl());
		assertEquals("http://b.hatena.ne.jp", addedFeed.getSiteUrl());
		assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed.getIconPath());
	}

	@Test
	public void testParseFeedInfoRSS2() {
		NetworkTaskManager networkTaskManager = NetworkTaskManager.getInstance(getContext());
		networkTaskManager.addNewFeed("http://hiroki.jp/feed/");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Feed addedFeed = adapter.getFeedByUrl("http://hiroki.jp/feed/");
//		ArrayList<Feed> feeds = adapter.getAllFeedsWithNumOfUnreadArticles();
		assertNotNull(addedFeed);
		assertEquals("http://hiroki.jp/feed/", addedFeed.getUrl());
		assertEquals("http://hiroki.jp", addedFeed.getSiteUrl());
		assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed.getIconPath());

		networkTaskManager.addNewFeed("http://www.infoq.com/jp/feed");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Feed infoqFeed = adapter.getFeedByUrl("http://www.infoq.com/jp/feed");
//		ArrayList<Feed> feeds = adapter.getAllFeedsWithNumOfUnreadArticles();
		assertNotNull(infoqFeed);
		assertEquals("http://www.infoq.com/jp/feed", infoqFeed.getUrl());
		assertEquals("http://www.infoq.com", infoqFeed.getSiteUrl());
		assertEquals(Feed.DEDAULT_ICON_PATH, infoqFeed.getIconPath());

		//http://blog.riywo.com/feed
		//http://dev.classmethod.jp/feed/
		//http://ggsoku.com/feed
		//http://labs.gree.jp/blog/feed
		//http://htcsoku.info/feed/
		//http://developer.hatenastaff.com/rss
		//http://rss.rssad.jp/rss/itmtop/2.0/itmedia_all.xml
		//http://developers.linecorp.com/blog/ja/?feed=rss2
	}

	@Test
	public void testParseFeedInfoATOM() {
		// Publickey
		String publicKeyFeedUrl = "http://www.publickey1.jp/atom.xml";
		NetworkTaskManager networkTaskManager = NetworkTaskManager.getInstance(getContext());
		networkTaskManager.addNewFeed(publicKeyFeedUrl);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Feed publicKeyFeed = adapter.getFeedByUrl(publicKeyFeedUrl);
		assertNotNull(publicKeyFeed);
		assertEquals("Publickey", publicKeyFeed.getTitle());
		assertEquals(publicKeyFeedUrl, publicKeyFeed.getUrl());
		assertEquals("http://www.publickey1.jp/", publicKeyFeed.getSiteUrl());
		assertEquals(Feed.DEDAULT_ICON_PATH, publicKeyFeed.getIconPath());

		// Google testing blog
		networkTaskManager.addNewFeed("http://feeds.feedburner.com/blogspot/RLXA");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Feed googleTestFeed = adapter.getFeedByUrl("http://feeds.feedburner.com/blogspot/RLXA");
		assertNotNull(googleTestFeed);
		assertEquals("http://feeds.feedburner.com/blogspot/RLXA", googleTestFeed.getUrl());
		assertEquals("http://testing.googleblog.com/", googleTestFeed.getSiteUrl());
		assertEquals(Feed.DEDAULT_ICON_PATH, googleTestFeed.getIconPath());

		// MOONGIFT
		networkTaskManager.addNewFeed("http://feeds.feedburner.com/moongift");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Feed monngiftFeed = adapter.getFeedByUrl("http://feeds.feedburner.com/moongift");
		assertNotNull(monngiftFeed);
		assertEquals("http://feeds.feedburner.com/moongift", monngiftFeed.getUrl());
		assertEquals("http://www.moongift.jp/", monngiftFeed.getSiteUrl());
		assertEquals(Feed.DEDAULT_ICON_PATH, monngiftFeed.getIconPath());
	}

	@Test
	public void testParseFeedInfoTopHtml() {
		// Test top URL
		NetworkTaskManager.getInstance(getContext()).addNewFeed("http://gigazine.net");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Feed addedFeed = adapter.getFeedByUrl("http://gigazine.net/news/rss_2.0/");
		assertNotNull(addedFeed);
		assertEquals("http://gigazine.net/news/rss_2.0/", addedFeed.getUrl());
		assertEquals("http://gigazine.net", addedFeed.getSiteUrl());
		assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed.getIconPath());
	}

	@Test
	public void testParseFeedInfoTopHtml2() {
		NetworkTaskManager.getInstance(getContext()).addNewFeed("http://tech.mercari.com/");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Feed mercariFeed = adapter.getFeedByUrl("http://tech.mercari.com/rss");
//		ArrayList<Feed> allFeeds = adapter.getAllFeedsThatHaveUnreadArticles();

		assertNotNull(mercariFeed);
		assertEquals("http://tech.mercari.com/rss", mercariFeed.getUrl());
		assertEquals("http://tech.mercari.com", mercariFeed.getSiteUrl());
		assertEquals(Feed.DEDAULT_ICON_PATH, mercariFeed.getIconPath());
	}

	@Test
	public void testParseFeedInfoTopHtmlFeedURLStartWithSlash() {
		// //smhn.info/feed is returned
		NetworkTaskManager.getInstance(getContext()).addNewFeed("http://smhn.info");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Feed smhnFeed = adapter.getFeedByUrl("http://smhn.info/feed");

		assertNotNull(smhnFeed);
		assertEquals("http://smhn.info/feed", smhnFeed.getUrl());
		assertEquals("http://smhn.info", smhnFeed.getSiteUrl());
		assertEquals(Feed.DEDAULT_ICON_PATH, smhnFeed.getIconPath());
	}

	@Test
	public void testParseFeedInfoGzip() {
		NetworkTaskManager.getInstance(getContext()).addNewFeed("http://ground-sesame.hatenablog.jp");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Feed surigomaFeed = adapter.getFeedByUrl("http://ground-sesame.hatenablog.jp/rss");
		assertNotNull(surigomaFeed);
		assertEquals("http://ground-sesame.hatenablog.jp/rss", surigomaFeed.getUrl());
		assertEquals("http://ground-sesame.hatenablog.jp", surigomaFeed.getSiteUrl());

		assertEquals(Feed.DEDAULT_ICON_PATH, surigomaFeed.getIconPath());
	}

	@Test
	public void testPathOnlyUrl() {
		addNewFeedAndCheckResult("http://b.hatena.ne.jp/hotentry/game", "http://b.hatena.ne.jp/hotentry/game.rss", "http://b.hatena.ne.jp");
	}

	private void addNewFeedAndCheckResult(String testUrl, String expectedFeedUrl, String expectedSiteUrl) {
		NetworkTaskManager.getInstance(getContext()).addNewFeed(testUrl);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Feed addedFeed = adapter.getFeedByUrl(expectedFeedUrl);
		assertNotNull(addedFeed);
		assertEquals(expectedFeedUrl, addedFeed.getUrl());
		assertEquals(expectedSiteUrl, addedFeed.getSiteUrl());

		assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed.getIconPath());
	}
}
