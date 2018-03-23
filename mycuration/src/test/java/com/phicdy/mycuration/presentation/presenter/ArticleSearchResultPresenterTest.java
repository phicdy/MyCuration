package com.phicdy.mycuration.presentation.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.data.rss.Article;
import com.phicdy.mycuration.presentation.view.ArticleSearchResultView;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class ArticleSearchResultPresenterTest {

    @Test
    public void WhenNotSearchIntentComesResultIsEmpty() {
        // Test data
        String testQuery = "hoge";
        ArrayList<Article> testResult = new ArrayList<>();
        testResult.add(new Article(1, "article1", "http://www.google.com/1", Article.UNREAD, "1", 1, 1, "testFeed1", ""));
        testResult.add(new Article(2, "article2", "http://www.google.com/2", Article.READ, "2", 2, 2, "testFeed2", ""));

        // Mock database has test result
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        Mockito.when(adapter.searchArticles(testQuery, true)).thenReturn(testResult);

        MockView view = new MockView();
        ArticleSearchResultPresenter presenter = new ArticleSearchResultPresenter(true, true, adapter);
        presenter.setView(view);
        presenter.handleIntent("invalidAction", testQuery);
        assertEquals(view.articles.size(), 0);
    }

    @Test
    public void WhenSearchIntentComesWithQueryListShowsSameSizeResultFromDb() {
        // Test data
        String testQuery = "hoge";
        ArrayList<Article> testResult = new ArrayList<>();
        testResult.add(new Article(1, "article1", "http://www.google.com/1", Article.UNREAD, "1", 1, 1, "testFeed1", ""));
        testResult.add(new Article(2, "article2", "http://www.google.com/2", Article.READ, "2", 2, 2, "testFeed2", ""));

        // Mock database has test result
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        Mockito.when(adapter.searchArticles(testQuery, true)).thenReturn(testResult);

        MockView view = new MockView();
        ArticleSearchResultPresenter presenter = new ArticleSearchResultPresenter(true, true, adapter);
        presenter.setView(view);
        presenter.handleIntent(Intent.ACTION_SEARCH, testQuery);
        assertEquals(view.articles.size(), 2);
    }

    @Test
    public void WhenItemIsClickedWithIntenalOptionIntenalWebViewOpens() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArticleSearchResultPresenter presenter = new ArticleSearchResultPresenter(true, true, adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.onListViewItemClicked("http://www.google.com");
        assertTrue(view.wasOpenedInternally);
    }

    @Test
    public void WhenItemIsClickedWithExtenalOptionExtenalWebViewOpens() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArticleSearchResultPresenter presenter = new ArticleSearchResultPresenter(true, false, adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.onListViewItemClicked("http://www.google.com");
        assertTrue(view.wasOpenedExternally);
    }

    @Test
    public void WhenItemIsOnLongClickedShareUiOpens() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArticleSearchResultPresenter presenter = new ArticleSearchResultPresenter(true, false, adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.onListViewItemLongClick("http://www.google.com");
        assertTrue(view.wasShareUiShowed);
    }

    @Test
    public void onCreateTest() {
        // For coverage
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArticleSearchResultPresenter presenter = new ArticleSearchResultPresenter(true, false, adapter);
        presenter.create();
    }

    @Test
    public void onResumeTest() {
        // For coverage
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArticleSearchResultPresenter presenter = new ArticleSearchResultPresenter(true, false, adapter);
        presenter.resume();
    }

    @Test
    public void onPauseTest() {
        // For coverage
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArticleSearchResultPresenter presenter = new ArticleSearchResultPresenter(true, false, adapter);
        presenter.pause();
    }

    private class MockView implements ArticleSearchResultView {

        private ArrayList<Article> articles = new ArrayList<>();
        private boolean wasOpenedInternally = false;
        private boolean wasOpenedExternally = false;
        private boolean wasShareUiShowed = false;

        @Override
        public void refreshList(@NonNull ArrayList<Article> articles) {
            this.articles = articles;
        }

        @Override
        public void startInternalWebView(@NonNull String url) {
            wasOpenedInternally = true;
        }

        @Override
        public void startExternalWebView(@NonNull String url) {
            wasOpenedExternally = true;
        }

        @Override
        public void startShareUrl(@NonNull String url) {
            wasShareUiShowed = true;
        }
    }
}
