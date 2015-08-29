package com.phicdy.filfeed.db.test;

import android.test.AndroidTestCase;

import com.phicdy.filfeed.db.DatabaseAdapter;
import com.phicdy.filfeed.rss.Article;
import com.phicdy.filfeed.rss.Feed;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class DatabaseAdapterTest extends AndroidTestCase {

	private DatabaseAdapter adapter;
	private ArrayList<Article> testUnreadArticles = new ArrayList<>();
	private ArrayList<Article> testReadArticles = new ArrayList<>();

	private static final String TEST_FEED_TITLE = "testfeed";
	private static final String TEST_FEED_URL = "http://www.yahoo.co.jp";
	private static final String TEST_ARTICLE1_TITLE = "title1";
	private final String TEST_CURATION_NAME = "test";
	private final String TEST_WORD1 = "word1";
	private final String TEST_WORD2 = "word2";
	private final String TEST_WORD3 = "word3";
	private final int TEST_WORDS_SIZE = 3;

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

	public void testSaveNewCuration() {
		insertTestCuration();

		int curationId = adapter.getCurationIdByName(TEST_CURATION_NAME);
		Map<Integer, ArrayList<String>> map = adapter.getAllCurationWords();
		assertTrue(map.containsKey(curationId));
		ArrayList<String> addedWords = map.get(curationId);
		assertEquals(TEST_WORDS_SIZE, addedWords.size());
		assertEquals(TEST_WORD1, addedWords.get(0));
		assertEquals(TEST_WORD2, addedWords.get(1));
		assertEquals(TEST_WORD3, addedWords.get(2));
	}

	public void testGetAllCurationWords() {
		// No data
		Map<Integer, ArrayList<String>> map = adapter.getAllCurationWords();
		assertEquals(0, map.size());

		// 1 curation
		insertTestCuration();
		int curationId1 = adapter.getCurationIdByName(TEST_CURATION_NAME);

		map = adapter.getAllCurationWords();
		assertEquals(1, map.size());
		assertTrue(map.containsKey(curationId1));
		ArrayList<String> addedWords1 = map.get(curationId1);
		assertEquals(TEST_WORD1, addedWords1.get(0));
		assertEquals(TEST_WORD2, addedWords1.get(1));
		assertEquals(TEST_WORD3, addedWords1.get(2));

		// 2 curations
		final String curationName2 = "test2";
		final String testWord4 = "word4";
		final String testWord5 = "word5";
		final String testWord6 = "word6";
		ArrayList<String> words2 = new ArrayList<>();
		words2.add(testWord4);
		words2.add(testWord5);
		words2.add(testWord6);
		assertTrue(adapter.saveNewCuration(curationName2, words2));
		int curationId2 = adapter.getCurationIdByName(curationName2);

		map = adapter.getAllCurationWords();
		assertEquals(2, map.size());

		assertTrue(map.containsKey(curationId1));
		addedWords1 = map.get(curationId1);
		assertEquals(TEST_WORD1, addedWords1.get(0));
		assertEquals(TEST_WORD2, addedWords1.get(1));
		assertEquals(TEST_WORD3, addedWords1.get(2));

		assertTrue(map.containsKey(curationId2));
		ArrayList<String> addedWords2 = map.get(curationId2);
		assertEquals(testWord4, addedWords2.get(0));
		assertEquals(testWord5, addedWords2.get(1));
		assertEquals(testWord6, addedWords2.get(2));
	}

	public void testDeleteCuration() {
		insertTestCuration();
		int curationId = adapter.getCurationIdByName(TEST_CURATION_NAME);
		assertTrue(adapter.deleteCuration(curationId));
		assertEquals(DatabaseAdapter.NOT_FOUND_ID, adapter.getCurationIdByName(TEST_CURATION_NAME));
	}

	public void testDeleteAllCuration() {
		insertTestCuration();
		assertTrue(adapter.deleteAllCuration());
		Map<Integer, ArrayList<String>> map = adapter.getAllCurationWords();
		assertEquals(0, map.size());
	}

	private void insertTestCuration() {
		ArrayList<String> words = new ArrayList<>();
		words.add(TEST_WORD1);
		words.add(TEST_WORD2);
		words.add(TEST_WORD3);
		assertTrue(adapter.saveNewCuration(TEST_CURATION_NAME, words));
	}

	@Override
	protected void tearDown() {
		adapter.deleteAllArticles();
		adapter.deleteAllFeeds();
		adapter.deleteAllCuration();
	}
}
