package com.phicdy.filfeed.db.test;

import android.test.AndroidTestCase;

import com.phicdy.filfeed.db.DatabaseAdapter;
import com.phicdy.filfeed.rss.Article;
import com.phicdy.filfeed.rss.Feed;

import java.util.ArrayList;
import java.util.Date;

public class DatabaseAdapterTest extends AndroidTestCase {

	private DatabaseAdapter adapter;
	private ArrayList<Article> testUnreadArticles = new ArrayList<>();
	private ArrayList<Article> testReadArticles = new ArrayList<>();

	private static final String TEST_FEED_TITLE = "testfeed";
	private static final String TEST_FEED_URL = "http://www.yahoo.co.jp";
	private static final String TEST_ARTICLE1_TITLE = "title1";

	public DatabaseAdapterTest() {
		super();
	}

	@Override
	protected void setUp() {
		adapter  = DatabaseAdapter.getInstance(getContext());
		adapter.deleteAllArticles();
		adapter.deleteAllFeeds();
		insertTestData();
	}

	public void testIsArticle() {
		for (Article testArticle : testUnreadArticles) {
			assertEquals(true, adapter.isArticle(testArticle));
		}

	}

	public void testSaveNewArticles() {
		ArrayList<Article> savedArticles = adapter.getAllArticles(false);
		if(savedArticles.size() != 0) {
			Article savedArticle1 = savedArticles.get(0);
			assertEquals(TEST_ARTICLE1_TITLE, savedArticle1.getTitle());
			assertEquals(Article.UNREAD, savedArticle1.getStatus());

			Article savedArticle2 = savedArticles.get(1);
			assertEquals("readArticle", savedArticle2.getTitle());
			assertEquals(Article.READ, savedArticle2.getStatus());

			Article savedArticle3 = savedArticles.get(2);
			assertEquals("title'", savedArticle3.getTitle());
			assertEquals(Article.UNREAD, savedArticle3.getStatus());

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

		adapter.saveNewFeed(TEST_FEED_TITLE, TEST_FEED_URL, "RSS", TEST_FEED_URL);
		Feed testFeed = adapter.getFeedByUrl(TEST_FEED_URL);
		
		long now = new Date().getTime();
		Article article = new Article(1, TEST_ARTICLE1_TITLE, "http://www.google.com",
				Article.UNREAD, "", now, testFeed.getId(), "");
		Article quotationTitle = new Article(1, "title'",
				"http://www.google.com", Article.UNREAD, "", now + 1, testFeed.getId(), "");
		Article doubleQuotationTitle = new Article(1, "title" + '"',
				"http://www.google.com", Article.UNREAD, "", now + 2, testFeed.getId(), "");
		Article japaneseTitle = new Article(1, "記事1abdｄｆｇ",
				"http://www.google.com", Article.UNREAD, "", now + 2, testFeed.getId(), "");

		testUnreadArticles.clear();
		testUnreadArticles.add(article);
		testUnreadArticles.add(quotationTitle);
		testUnreadArticles.add(doubleQuotationTitle);
		testUnreadArticles.add(japaneseTitle);
		adapter.saveNewArticles(testUnreadArticles, testFeed.getId());
		adapter.updateUnreadArticleCount(testFeed.getId(), testUnreadArticles.size());

		Article readArticle = new Article(1, "readArticle", "http://www.google.com/read",
				Article.READ, "", now, testFeed.getId(), "");
		testReadArticles.clear();
		testReadArticles.add(readArticle);
		adapter.saveNewArticles(testReadArticles, testFeed.getId());
	}

    public void testSaveAllStatusToReadFromToRead() {
        Feed testFeed = adapter.getFeedByUrl(TEST_FEED_URL);

        ArrayList<Article> articles = new ArrayList<>();
        long now = System.currentTimeMillis();
        Article toReadArticle = new Article(1, "toread_article",
                "http://www.google.com", Article.TOREAD, "", now + 1, testFeed.getId(), "");
        Article toReadArticle2 = new Article(1, "toread_article2",
                "http://www.google.com/hogehoge", Article.TOREAD, "", now + 2, testFeed.getId(), "");
        articles.add(toReadArticle);
        articles.add(toReadArticle2);
        adapter.saveNewArticles(articles, testFeed.getId());

        adapter.saveAllStatusToReadFromToRead();
        ArrayList<Article> changedArticles = adapter.getAllArticles(true);
        boolean existToReadArticle = false;
        for (Article article : changedArticles) {
            if (article.getStatus().equals(Article.TOREAD)) {
                existToReadArticle = true;
            }
        }
        assertEquals(false, existToReadArticle);
    }

	public void testGetAllFeedsWithNumOfUnreadArticles() {
		ArrayList<Feed> feeds = adapter.getAllFeedsWithNumOfUnreadArticles();
		assertNotNull(feeds);
		assertEquals(1, feeds.size());
		Feed testFeed = feeds.get(0);
		assertEquals(TEST_FEED_TITLE, testFeed.getTitle());
		assertEquals(TEST_FEED_URL, testFeed.getUrl());
		assertEquals(testUnreadArticles.size(), testFeed.getUnreadAriticlesCount());
	}

	public void testDeleteAllCuration() {
		final String curationName = "test";
		final String testWord2 = "word2";
		final String testWord3 = "word3";
		ArrayList<String> words = new ArrayList<>();
		words.add(TEST_ARTICLE1_TITLE);
		words.add(testWord2);
		words.add(testWord3);
		assertTrue(adapter.saveNewCuration(curationName, words));
		assertTrue(adapter.deleteAllCuration());
		Map<Integer, ArrayList<String>> map = adapter.getAllCurationWords();
		assertEquals(0, map.size());
	}

	@Override
	protected void tearDown() {
		adapter.deleteAllArticles();
		adapter.deleteAllFeeds();
	}
}
