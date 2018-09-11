package com.phicdy.mycuration.data.db;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.SparseArray;

import com.phicdy.mycuration.data.rss.Article;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.data.filter.Filter;
import com.phicdy.mycuration.presentation.view.activity.TopActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class DatabaseAdapterTest {

	private DatabaseAdapter adapter;
	private final ArrayList<Article> testUnreadArticles = new ArrayList<>();
	private final ArrayList<Article> testReadArticles = new ArrayList<>();

	private static final String TEST_FEED_TITLE = "testfeed";
	private static final String TEST_FEED_URL = "http://www.yahoo.co.jp";
	private static final String TEST_ARTICLE1_TITLE = "title1";
	private static final String TEST_ARTICLE2_TITLE = "title'";
	private static final String TEST_ARTICLE3_TITLE = "title" + '"';
	private final String TEST_CURATION_NAME = "test";
	private final String TEST_WORD1 = "word1";
	private final String TEST_WORD2 = "word2";
	private final String TEST_WORD3 = "word3";

	public DatabaseAdapterTest() {
		super();
	}

    @Rule
    public ActivityTestRule<TopActivity> mActivityRule = new ActivityTestRule<>(
            TopActivity.class);

    @Before
	public void setUp() {
		DatabaseAdapter.setUp(new DatabaseHelper(getTargetContext()));
		adapter  = DatabaseAdapter.getInstance();
		insertTestData();
	}

    @After
    public void tearDown() {
        adapter.deleteAll();
    }

    @Test
	public void testIsArticle() {
		for (Article testArticle : testUnreadArticles) {
			assertEquals(true, adapter.isArticle(testArticle));
		}

	}

    @Test
	public void testSaveNewArticles() {
		// Reset data and insert curation at first
		adapter.deleteAll();
		insertTestCurationForArticle1();
		insertTestData();

		ArrayList<Article> savedArticles = adapter.getTop300Articles(false);
		if(savedArticles.size() != 0) {
			Article savedArticle1 = savedArticles.get(0);
			assertEquals(TEST_ARTICLE1_TITLE, savedArticle1.getTitle());
			assertEquals(Article.UNREAD, savedArticle1.getStatus());

			Article savedArticle2 = savedArticles.get(1);
			assertEquals(TEST_ARTICLE2_TITLE, savedArticle2.getTitle());
			assertEquals(Article.UNREAD, savedArticle2.getStatus());

			Article savedArticle3 = savedArticles.get(2);
			assertEquals(TEST_ARTICLE3_TITLE, savedArticle3.getTitle());
			assertEquals(Article.UNREAD, savedArticle3.getStatus());
		}

		int curatioId = adapter.getCurationIdByName(TEST_CURATION_NAME);
		ArrayList<Article> articles = adapter.getAllArticlesOfCuration(curatioId, true);
		assertNotNull(articles);
		assertEquals(1, articles.size());
		assertEquals(TEST_ARTICLE1_TITLE, articles.get(0).getTitle());
	}

    @Test
	public void testSearchArticles() {
		ArrayList<Article> list = adapter.searchArticles("記事1abdｄｆｇ", true);
		assertEquals(1, list.size());
		assertEquals("記事1abdｄｆｇ", list.get(0).getTitle());
	}
	
	private void insertTestData() {
		adapter.saveNewFeed(TEST_FEED_TITLE, TEST_FEED_URL, "RSS", TEST_FEED_URL);
		Feed testFeed = adapter.getFeedByUrl(TEST_FEED_URL);
		
		long now = new Date().getTime();
		Article article = new Article(1, TEST_ARTICLE1_TITLE, "http://www.google.com",
				Article.UNREAD, "", now, testFeed.getId(), "", "");
		Article quotationTitle = new Article(1, TEST_ARTICLE2_TITLE,
				"http://www.google.com", Article.UNREAD, "", now + 1, testFeed.getId(), "", "");
		Article doubleQuotationTitle = new Article(1, TEST_ARTICLE3_TITLE,
				"http://www.google.com", Article.UNREAD, "", now + 2, testFeed.getId(), "", "");
		Article japaneseTitle = new Article(1, "記事1abdｄｆｇ",
				"http://www.google.com", Article.UNREAD, "", now + 2, testFeed.getId(), "", "");

		testUnreadArticles.clear();
		testUnreadArticles.add(article);
		testUnreadArticles.add(quotationTitle);
		testUnreadArticles.add(doubleQuotationTitle);
		testUnreadArticles.add(japaneseTitle);
		adapter.saveNewArticles(testUnreadArticles, testFeed.getId());
		adapter.updateUnreadArticleCount(testFeed.getId(), testUnreadArticles.size());

		Article readArticle = new Article(1, "readArticle", "http://www.google.com/read",
				Article.READ, "", now, testFeed.getId(), "", "");
		testReadArticles.clear();
		testReadArticles.add(readArticle);
		adapter.saveNewArticles(testReadArticles, testFeed.getId());
	}

    @Test
    public void testSaveAllStatusToReadFromToRead() {
        Feed testFeed = adapter.getFeedByUrl(TEST_FEED_URL);

        ArrayList<Article> articles = new ArrayList<>();
        long now = System.currentTimeMillis();
        Article toReadArticle = new Article(1, "toread_article",
                "http://www.google.com", Article.TOREAD, "", now + 1, testFeed.getId(), "", "");
        Article toReadArticle2 = new Article(1, "toread_article2",
                "http://www.google.com/hogehoge", Article.TOREAD, "", now + 2, testFeed.getId(), "", "");
        articles.add(toReadArticle);
        articles.add(toReadArticle2);
        adapter.saveNewArticles(articles, testFeed.getId());

        adapter.saveAllStatusToReadFromToRead();
        ArrayList<Article> changedArticles = adapter.getTop300Articles(true);
        boolean existToReadArticle = false;
        for (Article article : changedArticles) {
            if (article.getStatus().equals(Article.TOREAD)) {
                existToReadArticle = true;
            }
        }
        assertEquals(false, existToReadArticle);
    }

    @Test
	public void testGetAllFeedsWithNumOfUnreadArticles() {
		ArrayList<Feed> feeds = adapter.getAllFeedsWithNumOfUnreadArticles();
		assertNotNull(feeds);
		assertEquals(1, feeds.size());
		Feed testFeed = feeds.get(0);
		assertEquals(TEST_FEED_TITLE, testFeed.getTitle());
		assertEquals(TEST_FEED_URL, testFeed.getUrl());
		assertEquals(testUnreadArticles.size(), testFeed.getUnreadAriticlesCount());
	}

    @Test
	public void testSaveNewCuration() {
		insertTestCuration();

		int curationId = adapter.getCurationIdByName(TEST_CURATION_NAME);
		SparseArray<ArrayList<String>> map = adapter.getAllCurationWords();
		assertThat(map.indexOfKey(curationId), is(0));
        assertThat(map.size(), is(1));
		ArrayList<String> addedWords = map.get(curationId);
		final int TEST_WORDS_SIZE = 3;
		assertEquals(TEST_WORDS_SIZE, addedWords.size());
		assertEquals(TEST_WORD1, addedWords.get(0));
		assertEquals(TEST_WORD2, addedWords.get(1));
		assertEquals(TEST_WORD3, addedWords.get(2));
	}

    @Test
	public void testGetAllCurationWords() {
		// No data
		SparseArray<ArrayList<String>> map = adapter.getAllCurationWords();
		assertEquals(0, map.size());

		// 1 curation
		insertTestCuration();
		int curationId1 = adapter.getCurationIdByName(TEST_CURATION_NAME);

		map = adapter.getAllCurationWords();
		assertEquals(1, map.size());
		assertThat(map.indexOfKey(curationId1), is(0));
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

		assertThat(map.indexOfKey(curationId1), is(0));
		addedWords1 = map.get(curationId1);
		assertEquals(TEST_WORD1, addedWords1.get(0));
		assertEquals(TEST_WORD2, addedWords1.get(1));
		assertEquals(TEST_WORD3, addedWords1.get(2));

		assertThat(map.indexOfKey(curationId2), is(1));
		ArrayList<String> addedWords2 = map.get(curationId2);
		assertEquals(testWord4, addedWords2.get(0));
		assertEquals(testWord5, addedWords2.get(1));
		assertEquals(testWord6, addedWords2.get(2));
	}

    @Test
	public void testDeleteCuration() {
		insertTestCuration();
		int curationId = adapter.getCurationIdByName(TEST_CURATION_NAME);
		assertTrue(adapter.deleteCuration(curationId));
		assertEquals(DatabaseAdapter.NOT_FOUND_ID, adapter.getCurationIdByName(TEST_CURATION_NAME));
	}

    @Test
	public void testDeleteAllCuration() {
		insertTestCuration();
		assertTrue(adapter.deleteAllCuration());
		SparseArray<ArrayList<String>> map = adapter.getAllCurationWords();
		assertEquals(0, map.size());
	}

	@Test
    public void deleteFeed() {
        // Set test filter
        Feed feed = adapter.getFeedByUrl(TEST_FEED_URL);
        ArrayList<Feed> feeds = new ArrayList<>();
        feeds.add(feed);
        adapter.saveNewFilter("hoge", feeds, "keyword", "");
        Feed feed2 = adapter.saveNewFeed("testfeed2", "http://www.hoge.com", "RSS", "http://www.hoge.com");
        feeds.add(feed2);
        adapter.saveNewFilter("hoge2", feeds, "keyword2", "");

        // Delete feed
        int feedId = feed.getId();
        assertTrue(adapter.deleteFeed(feedId));

        // Check the feed and related data was deleted
        feed = adapter.getFeedById(feedId);
        assertNull(feed);
        ArrayList<Article> articles = adapter.getAllArticlesInAFeed(feedId, true);
        assertNotNull(articles);
        assertThat(articles.size(), is(0));
        ArrayList<Filter> filters = adapter.getEnabledFiltersOfFeed(feedId);
        assertNotNull(filters);
        assertThat(filters.size(), is(0));

        // Check filter that contains other feed was not deleted
        filters = adapter.getEnabledFiltersOfFeed(feed2.getId());
        assertNotNull(filters);
        assertThat(filters.size(), is(1));
        Filter filter = filters.get(0);
        assertThat(filter.getTitle(), is("hoge2"));
        assertThat(filter.getKeyword(), is("keyword2"));
    }

    @Test
	public void testGetAllArticlesOfCuration() {
		insertTestCurationForArticle1();
		int curationId = adapter.getCurationIdByName(TEST_CURATION_NAME);

		assertTrue(adapter.adaptCurationToArticles(TEST_CURATION_NAME, getWordsOfCurationForArticle1()));
		ArrayList<Article> articles = adapter.getAllArticlesOfCuration(curationId, true);
		assertNotNull(articles);
		assertEquals(1, articles.size());
		assertEquals(TEST_ARTICLE1_TITLE, articles.get(0).getTitle());
	}

	private void insertTestCuration() {
		ArrayList<String> words = new ArrayList<>();
		words.add(TEST_WORD1);
		words.add(TEST_WORD2);
		words.add(TEST_WORD3);
		assertTrue(adapter.saveNewCuration(TEST_CURATION_NAME, words));
	}

	private void insertTestCurationForArticle1() {
		ArrayList<String> words = new ArrayList<>();
		words.add(TEST_ARTICLE1_TITLE);
		words.add(TEST_WORD2);
		words.add(TEST_WORD3);
		assertTrue(adapter.saveNewCuration(TEST_CURATION_NAME, words));
	}

	private ArrayList<String> getWordsOfCurationForArticle1() {
		ArrayList<String> words = new ArrayList<>();
		words.add(TEST_ARTICLE1_TITLE);
		words.add(TEST_WORD2);
		words.add(TEST_WORD3);
		return words;
	}

}
