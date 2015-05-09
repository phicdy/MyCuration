package com.pleua.rssfilterreader.rss.test;

import java.io.IOException;
import java.io.InputStream;

import android.test.AndroidTestCase;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.rss.Feed;
import com.pluea.filfeed.rss.RssParser;
import com.pluea.filfeed.task.InputStreamRequest;

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
		
		// Test top URL
//		try {
//			Feed addedFeed = parser.parseFeedInfo("http://jp.techcrunch.com");
//				Thread.sleep(5000);
//			
//			assertNotNull(addedFeed);
//			assertEquals("http://jp.techcrunch.com/feed/", addedFeed.getUrl());
//			assertEquals("http://jp.techcrunch.com", addedFeed.getSiteUrl());
//			assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed.getIconPath());
//		} catch (IOException e) {
//			e.printStackTrace();
//			fail();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			Feed surigomaFeed = parser.parseFeedInfo("http://ground-sesame.hatenablog.jp");
//			Thread.sleep(7000);
//			
//			assertNotNull(surigomaFeed);
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

	public void testParseXml() {
		final DatabaseAdapter adapter = DatabaseAdapter.getInstance(getContext());

		// Add a test feed
		adapter.saveNewFeed("Publickey","http://www.publickey1.jp/atom.xml", "", "http://www.publickey1.jp/");
		String publicKeyUrl = "http://www.publickey1.jp/atom.xml";
		final Feed feed = adapter.getFeedByUrl(publicKeyUrl);
		if (feed == null) {
			fail("PublicKey feed is not found");
		}
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
					            RssParser parser = new RssParser(getContext()); 
					            boolean result =  false;
					            try {
									result = parser.parseXml(in, feed.getId());
								} catch (IOException e) {
									e.printStackTrace();
									fail();
								} finally {
									try {
										in.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
									assertEquals(true, result);
								}
					    	}
			        	}).start();
			        }  
			    }, new ErrorListener() {  
			  
			        @Override  
			        public void onErrorResponse(VolleyError error) {  
			        	Log.d("LOG_TAG", "Request error:" + error.getMessage());
			        	fail();
			        }  
			    });  
			  
		RequestQueue mQueue = Volley.newRequestQueue(getContext());
		mQueue.add(request);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
