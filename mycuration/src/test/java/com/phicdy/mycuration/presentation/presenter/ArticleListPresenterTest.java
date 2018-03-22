package com.phicdy.mycuration.presentation.presenter;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.data.rss.Article;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.data.rss.UnreadCountManager;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.presentation.view.ArticleListView;
import com.phicdy.mycuration.util.PreferenceHelper;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class ArticleListPresenterTest {

    @Test
    public void testOnCreate() {
        // For coverage
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);
        ArticleListPresenter presenter = new ArticleListPresenter(
                1, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
    }

    @Test
    public void NoArticlesAreLoadedAfterOnCreateViewWithEmptyDBForFeed() {
        int testFeedId = 1;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock empty database
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        when(adapter.getUnreadArticlesInAFeed(testFeedId, true)).thenReturn(new ArrayList<Article>());
        when(adapter.isExistArticle(testFeedId)).thenReturn(false);

        ArticleListPresenter presenter = new ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        assertThat(presenter.articleSize(), is(0));
    }

    @Test
    public void NoArticlesAreLoadedAfterOnCreateViewWithEmptyDBForAllFeed() {
        int testFeedId = Feed.ALL_FEED_ID;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock empty database
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        when(adapter.getAllUnreadArticles(true)).thenReturn(new ArrayList<Article>());
        when(adapter.isExistArticle(testFeedId)).thenReturn(false);

        ArticleListPresenter presenter = new ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        assertThat(presenter.articleSize(), is(0));
    }

    @Test
    public void NoArticlesAreLoadedAfterOnCreateViewWithEmptyDBForCuration() {
        int testCurationId = 1;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock empty database
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        when(adapter.getAllUnreadArticlesOfCuration(testCurationId, true))
                .thenReturn(new ArrayList<Article>());
        when(adapter.getAllArticlesOfCuration(testCurationId, true))
                .thenReturn(new ArrayList<Article>());

        ArticleListPresenter presenter = new ArticleListPresenter(
                Feed.DEFAULT_FEED_ID, testCurationId, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        assertThat(presenter.articleSize(), is(0));
    }

    @Test
    public void AllArticlesOfFeedAreLoadedAfterOnCreateViewIfAlreadyRead() {
        int testFeedId = 1;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock 1 artcile exists but, it was already read
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        when(adapter.getUnreadArticlesInAFeed(testFeedId, true)).thenReturn(new ArrayList<Article>());
        when(adapter.isExistArticle(testFeedId)).thenReturn(true);
        ArrayList<Article> articles = new ArrayList<>();
        articles.add(new Article(1, "hoge", "http://www.google.com",
                Article.READ, "1", 1, 1, "feed", ""));
        when(adapter.getAllArticlesInAFeed(testFeedId, true))
                .thenReturn(articles);

        ArticleListPresenter presenter = new ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        assertThat(presenter.articleSize(), is(1));
    }

    @Test
    public void AllArticlesAreLoadedAfterOnCreateViewIfAlreadyRead() {
        int testFeedId = Feed.ALL_FEED_ID;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock 1 artcile exists but, it was already read
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        when(adapter.getAllUnreadArticles(true)).thenReturn(new ArrayList<Article>());
        when(adapter.isExistArticle()).thenReturn(true);
        ArrayList<Article> articles = new ArrayList<>();
        articles.add(new Article(1, "hoge", "http://www.google.com",
                Article.READ, "1", 1, 1, "feed", ""));
        when(adapter.getTop300Articles(true))
                .thenReturn(articles);

        ArticleListPresenter presenter = new ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        assertThat(presenter.articleSize(), is(1));
    }

    @Test
    public void AllArticlesOfCurationAreLoadedAfterOnCreateViewIfAlreadyRead() {
        int testCurationId = 1;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock 1 artcile exists but, it was already read
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        when(adapter.getAllUnreadArticlesOfCuration(testCurationId, true))
                .thenReturn(new ArrayList<Article>());
        ArrayList<Article> articles = new ArrayList<>();
        articles.add(new Article(1, "hoge", "http://www.google.com",
                Article.READ, "1", 1, 1, "feed", ""));
        when(adapter.getAllArticlesOfCuration(testCurationId, true))
                .thenReturn(articles);

        ArticleListPresenter presenter = new ArticleListPresenter(
                Feed.DEFAULT_FEED_ID, testCurationId, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        assertThat(presenter.articleSize(), is(1));
    }

    @Test
    public void AllUnreadArticlesOfFeedAreOnlyLoadedAfterOnCreateView() {
        int testFeedId = 1;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock 2 unread artcile and 2 read exists
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Article> unreadArticles = new ArrayList<>();
        unreadArticles.add(new Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", ""));
        unreadArticles.add(new Article(2, "unread2", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", ""));
        when(adapter.getUnreadArticlesInAFeed(testFeedId, true))
                .thenReturn(unreadArticles);
        when(adapter.isExistArticle(testFeedId)).thenReturn(true);
        ArrayList<Article> readArticles = new ArrayList<>();
        readArticles.add(new Article(3, "read", "http://www.google.com",
                Article.READ, "1", 1, 1, "feed", ""));
        readArticles.add(new Article(4, "read1", "http://www.google.com",
                Article.READ, "1", 1, 1, "feed", ""));
        when(adapter.getAllArticlesInAFeed(testFeedId, true))
                .thenReturn(readArticles);

        ArticleListPresenter presenter = new ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        assertTrue(presenter.isAllUnreadArticle());
    }

    @Test
    public void AllUnreadArticlesAreOnlyLoadedAfterOnCreateView() {
        int testFeedId = Feed.ALL_FEED_ID;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock 2 unread artcile and 2 read exists
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Article> unreadArticles = new ArrayList<>();
        unreadArticles.add(new Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", ""));
        unreadArticles.add(new Article(2, "unread2", "http://www.google.com",
                Article.UNREAD, "1", 1, 2, "feed", ""));
        when(adapter.getAllUnreadArticles(true))
                .thenReturn(unreadArticles);
        when(adapter.isExistArticle()).thenReturn(true);
        ArrayList<Article> readArticles = new ArrayList<>();
        readArticles.add(new Article(3, "read", "http://www.google.com",
                Article.READ, "1", 1, 3, "feed", ""));
        readArticles.add(new Article(4, "read1", "http://www.google.com",
                Article.READ, "1", 1, 4, "feed", ""));
        when(adapter.getTop300Articles(true))
                .thenReturn(readArticles);

        ArticleListPresenter presenter = new ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        assertTrue(presenter.isAllUnreadArticle());
    }

    @Test
    public void AllUnreadArticlesOfCurationAreOnlyLoadedAfterOnCreateView() {
        int testCurationId = 1;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock 2 unread artcile and 2 read exists
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Article> unreadArticles = new ArrayList<>();
        unreadArticles.add(new Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", ""));
        unreadArticles.add(new Article(2, "unread2", "http://www.google.com",
                Article.UNREAD, "1", 1, 2, "feed", ""));
        when(adapter.getAllUnreadArticlesOfCuration(testCurationId, true))
                .thenReturn(unreadArticles);
        ArrayList<Article> readArticles = new ArrayList<>();
        readArticles.add(new Article(3, "read", "http://www.google.com",
                Article.READ, "1", 1, 3, "feed", ""));
        readArticles.add(new Article(4, "read1", "http://www.google.com",
                Article.READ, "1", 1, 4, "feed", ""));
        when(adapter.getAllArticlesOfCuration(testCurationId, true))
                .thenReturn(readArticles);

        ArticleListPresenter presenter = new ArticleListPresenter(
                Feed.DEFAULT_FEED_ID, testCurationId, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        assertTrue(presenter.isAllUnreadArticle());
    }

    @Test
    public void testOnPause() {
        // For coverage
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);
        ArticleListPresenter presenter = new ArticleListPresenter(
                1, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.pause();
    }

    @Test
    public void UnreadArticleStatusBecomesToReadWhenClicked() {
        int testFeedId = 1;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock 1 unread artcile exists
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Article> unreadArticles = new ArrayList<>();
        Article clickedArticle = new Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "");
        unreadArticles.add(clickedArticle);
        when(adapter.getUnreadArticlesInAFeed(testFeedId, true))
                .thenReturn(unreadArticles);
        when(adapter.isExistArticle(testFeedId)).thenReturn(true);

        ArticleListPresenter presenter = new ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        presenter.onListItemClicked(0);
        assertThat(clickedArticle.getStatus(), is(Article.TOREAD));
    }

    @Test
    public void ToreadArticleStatusIsStillToReadWhenClicked() {
        int testFeedId = 1;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock 1 toread artcile exists
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Article> unreadArticles = new ArrayList<>();
        Article clickedArticle = new Article(1, "unread", "http://www.google.com",
                Article.TOREAD, "1", 1, 1, "feed", "");
        unreadArticles.add(clickedArticle);
        when(adapter.getUnreadArticlesInAFeed(testFeedId, true))
                .thenReturn(unreadArticles);
        when(adapter.isExistArticle(testFeedId)).thenReturn(true);

        ArticleListPresenter presenter = new ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        presenter.onListItemClicked(0);
        assertThat(clickedArticle.getStatus(), is(Article.TOREAD));
    }

    @Test
    public void ReadArticleStatusIsStillReadWhenClicked() {
        int testFeedId = 1;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock 1 read artcile exists
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Article> unreadArticles = new ArrayList<>();
        Article clickedArticle = new Article(1, "unread", "http://www.google.com",
                Article.READ, "1", 1, 1, "feed", "");
        unreadArticles.add(clickedArticle);
        when(adapter.getUnreadArticlesInAFeed(testFeedId, true))
                .thenReturn(unreadArticles);
        when(adapter.isExistArticle(testFeedId)).thenReturn(true);

        ArticleListPresenter presenter = new ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        presenter.onListItemClicked(0);
        assertThat(clickedArticle.getStatus(), is(Article.READ));
    }

    @Test
    public void ArticleIsOpenedWithInternalOptionWhenClicked() {
        int testFeedId = 1;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock 1 unread artcile exists
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Article> unreadArticles = new ArrayList<>();
        Article clickedArticle = new Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "");
        unreadArticles.add(clickedArticle);
        when(adapter.getUnreadArticlesInAFeed(testFeedId, true))
                .thenReturn(unreadArticles);
        when(adapter.isExistArticle(testFeedId)).thenReturn(true);

        ArticleListPresenter presenter = new ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        presenter.onListItemClicked(0);
        assertThat(view.openedUrl, is(clickedArticle.getUrl()));
    }

    @Test
    public void ArticleIsOpenedWithExternalOptionWhenClicked() {
        int testFeedId = 1;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock 1 unread artcile exists
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Article> unreadArticles = new ArrayList<>();
        Article clickedArticle = new Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "");
        unreadArticles.add(clickedArticle);
        when(adapter.getUnreadArticlesInAFeed(testFeedId, true))
                .thenReturn(unreadArticles);
        when(adapter.isExistArticle(testFeedId)).thenReturn(true);

        ArticleListPresenter presenter = new ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, false, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        presenter.onListItemClicked(0);
        assertThat(view.openedUrl, is(clickedArticle.getUrl()));
    }

    @Test
    public void IntenalWebViewIsOpenedWithInternalOptionWhenClicked() {
        int testFeedId = 1;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock 1 unread artcile exists
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Article> unreadArticles = new ArrayList<>();
        Article clickedArticle = new Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "");
        unreadArticles.add(clickedArticle);
        when(adapter.getUnreadArticlesInAFeed(testFeedId, true))
                .thenReturn(unreadArticles);
        when(adapter.isExistArticle(testFeedId)).thenReturn(true);

        ArticleListPresenter presenter = new ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        presenter.onListItemClicked(0);
        assertTrue(view.isOpenedInternalWebView);
    }

    @Test
    public void ExtenalWebViewIsOpenedWithExternalOptionWhenClicked() {
        int testFeedId = 1;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock 1 unread artcile exists
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Article> unreadArticles = new ArrayList<>();
        Article clickedArticle = new Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "");
        unreadArticles.add(clickedArticle);
        when(adapter.getUnreadArticlesInAFeed(testFeedId, true))
                .thenReturn(unreadArticles);
        when(adapter.isExistArticle(testFeedId)).thenReturn(true);

        ArticleListPresenter presenter = new ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, false, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        presenter.onListItemClicked(0);
        assertTrue(view.isOpenedExternalWebView);
    }

    @Test
    public void ShareUiShowsWhenLongClicked() {
        int testFeedId = 1;
        UnreadCountManager manager = Mockito.mock(UnreadCountManager.class);

        // Mock 1 unread artcile exists
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Article> unreadArticles = new ArrayList<>();
        Article longClickedArticle = new Article(1, "unread", "http://www.google.com",
                Article.UNREAD, "1", 1, 1, "feed", "");
        unreadArticles.add(longClickedArticle);
        when(adapter.getUnreadArticlesInAFeed(testFeedId, true))
                .thenReturn(unreadArticles);
        when(adapter.isExistArticle(testFeedId)).thenReturn(true);

        ArticleListPresenter presenter = new ArticleListPresenter(
                testFeedId, ArticleListPresenter.DEFAULT_CURATION_ID, adapter,
                manager, true, true, true, PreferenceHelper.SWIPE_LEFT_TO_RIGHT);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.createView();
        presenter.onListItemLongClicked(0);
        assertThat(view.shareUrl, is(longClickedArticle.getUrl()));
    }
    private class MockView implements ArticleListView {

        private boolean isOpenedInternalWebView = false;
        private boolean isOpenedExternalWebView = false;
        private String shareUrl;
        private String openedUrl;

        @Override
        public void openInternalWebView(@NonNull String url) {
            isOpenedInternalWebView = true;
            openedUrl = url;
        }

        @Override
        public void openExternalWebView(@NonNull String url) {
            isOpenedExternalWebView = true;
            openedUrl = url;
        }

        @Override
        public void notifyListView() {

        }

        @Override
        public void finish() {
        }

        @Override
        public int getFirstVisiblePosition() {
            return 0;
        }

        @Override
        public int getLastVisiblePosition() {
            return 0;
        }

        @Override
        public void showShareUi(@NonNull String url) {
            shareUrl = url;
        }

        @Override
        public void scrollTo(int position) {

        }

        @Override
        public boolean isBottomVisible() {
            return false;
        }

        @Override
        public void showEmptyView() {

        }
    }
}
