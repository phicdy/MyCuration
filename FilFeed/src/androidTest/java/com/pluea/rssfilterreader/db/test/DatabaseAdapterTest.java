package com.pluea.rssfilterreader.db.test;

import java.util.ArrayList;
import java.util.Date;

import android.test.AndroidTestCase;

import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.rss.Article;
import com.pluea.filfeed.rss.Feed;

public class DatabaseAdapterTest extends AndroidTestCase {

	private DatabaseAdapter adapter;
	private ArrayList<Article> testArticles;
	
	public DatabaseAdapterTest() {
		super();
	}
	
	protected void setUp() {
		adapter  = DatabaseAdapter.getInstance(getContext());
		insertTestData();
	}

	public void testIsArticle() {
		for (Article testArticle : testArticles) {
			assertEquals(true, adapter.isArticle(testArticle));
		}

	}

	public void testSaveNewArticles() {
		ArrayList<Article> savedArticles = adapter.getAllArticles(false);
		if(savedArticles.size() != 0) {
			Article savedArticle1 = savedArticles.get(0);
			assertEquals("title1", savedArticle1.getTitle());
			
			Article savedArticle2 = savedArticles.get(1);
			assertEquals("title'", savedArticle2.getTitle());
			
			Article savedArticle3 = savedArticles.get(2);
			assertEquals("title" + '"', savedArticle3.getTitle());
		}
	}
	
	public void testSearchArticles() {
		ArrayList<Article> list = adapter.searchArticles("記事1abdｄｆｇ", true);
		assertEquals(1, list.size());
		assertEquals("記事1abdｄｆｇ", list.get(0).getTitle());
	}
	
	public void testsaveStatusToRead() {
		
	}
	
	private void insertTestData() {
		DatabaseAdapter adapter = DatabaseAdapter.getInstance(getContext());

		adapter.saveNewFeed("testfeed", "http://www.yahoo.co.jp", "RSS", "http://www.yahoo.co.jp");
		Feed testFeed = adapter.getFeedByUrl("http://www.yahoo.co.jp");
		
		long now = new Date().getTime();
		Article article = new Article(1, "title1", "http://www.google.com",
				"unread", "", now, testFeed.getId(), "");
		Article quotationTitle = new Article(1, "title'",
				"http://www.google.com", "unread", "", now + 1, testFeed.getId(), "");
		Article doubleQuotationTitle = new Article(1, "title" + '"',
				"http://www.google.com", "unread", "", now + 2, testFeed.getId(), "");
		Article japaneseTitle = new Article(1, "記事1abdｄｆｇ",
				"http://www.google.com", "unread", "", now + 2, testFeed.getId(), "");

		testArticles = new ArrayList<Article>();
		testArticles.add(article);
		testArticles.add(quotationTitle);
		testArticles.add(doubleQuotationTitle);
		testArticles.add(japaneseTitle);
		adapter.saveNewArticles(testArticles, testFeed.getId());
	}
	
	private void deleteAllFeeds() {
		for (Feed feed : adapter.getAllFeedsWithoutNumOfUnreadArticles()) {
			adapter.deleteFeed(feed.getId());
		}
	}
	
	private void deleteAllArticles() {
		adapter.deleteAllArticles();
	}
	
	protected void tearDown() {
		deleteAllFeeds();
		deleteAllArticles();
	}
}
