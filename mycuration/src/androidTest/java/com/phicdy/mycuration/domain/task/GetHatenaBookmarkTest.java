package com.phicdy.mycuration.domain.task;

import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.data.db.DatabaseHelper;
import com.phicdy.mycuration.data.rss.Article;
import com.phicdy.mycuration.data.rss.Feed;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GetHatenaBookmarkTest {

    @Before
    public void setup() {
        DatabaseAdapter.setUp(new DatabaseHelper(getTargetContext()));
        DatabaseAdapter adapter = DatabaseAdapter.getInstance();
        adapter.deleteAll();
    }

    @After
    public void tearDown() {
        DatabaseAdapter adapter = DatabaseAdapter.getInstance();
        adapter.deleteAll();
    }

    @Test
    public void MyQiitaArticleReturns0() {
        DatabaseAdapter adapter = DatabaseAdapter.getInstance();
        Feed testFeed = adapter.saveNewFeed("test", "http://hoge.com", "hoge", "");
        ArrayList<Article> articles = new ArrayList<>();
        String testUrl = "http://qiita.com/phicdy/items/1bcce3d6f040fc48f7bf";
        articles.add(new Article(1, "hoge", testUrl, Article.UNREAD, "", 1, testFeed.getId(), "", ""));
        adapter.saveNewArticles(articles, testFeed.getId());

        GetHatenaBookmark getHatenaBookmark = new GetHatenaBookmark(adapter);
        getHatenaBookmark.request(testUrl, 0);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertThat(adapter.getAllUnreadArticles(true).get(0).getPoint(), is("0"));
    }

    @Test
    public void MyBlogArticleReturns1() {
        DatabaseAdapter adapter = DatabaseAdapter.getInstance();

        // Save test feed and article
        Feed testFeed = adapter.saveNewFeed("test", "http://hoge.com", "hoge", "");
        ArrayList<Article> articles = new ArrayList<>();
        String testUrl = "http://phicdy.hatenablog.com/entry/2014/09/01/214055";
        articles.add(new Article(1, "hoge", testUrl, Article.UNREAD, "", 1, testFeed.getId(), "", ""));
        adapter.saveNewArticles(articles, testFeed.getId());

        // Start request
        GetHatenaBookmark getHatenaBookmark = new GetHatenaBookmark(adapter);
        getHatenaBookmark.request(testUrl, 0);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertThat(adapter.getAllUnreadArticles(true).get(0).getPoint(), is("1"));
    }
}
