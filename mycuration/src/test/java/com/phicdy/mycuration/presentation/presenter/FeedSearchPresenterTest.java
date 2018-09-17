package com.phicdy.mycuration.presentation.presenter;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.data.db.DatabaseHelper;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.domain.rss.RssParseResult;
import com.phicdy.mycuration.domain.rss.RssParser;
import com.phicdy.mycuration.domain.rss.UnreadCountManager;
import com.phicdy.mycuration.domain.task.NetworkTaskManager;
import com.phicdy.mycuration.presentation.view.FeedSearchView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FeedSearchPresenterTest {

    private NetworkTaskManager networkTaskManager;
    private UnreadCountManager unreadCountManager;
    private RssParser parser;
    private FeedSearchPresenter presenter;

    @Before
    public void setup() {
        networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter.setUp(Mockito.mock(DatabaseHelper.class));
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        unreadCountManager = Mockito.mock(UnreadCountManager.class);
        parser = Mockito.mock(RssParser.class);
        presenter = new FeedSearchPresenter(
                networkTaskManager, adapter, unreadCountManager, parser);

    }

    @Test
    public void testOnCreate() {
        // For coverage
        presenter.setView(new MockView());
        presenter.create();
        assertTrue(true);
    }

    @Test
    public void testOnResume() {
        // For coverage
        presenter.setView(new MockView());
        presenter.create();
        presenter.resume();
        assertTrue(true);
    }

    @Test
    public void receiverIsUnregisteredInAfterPause() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.pause();
        assertFalse(view.isReceiverRegistered);
    }

    @Test
    public void feedHookActivityDoesNotShowWhenFabIsClickedWithEmpty() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.onFabClicked("");
        assertFalse(view.isFeedHookActivityForeground);
    }

    @Test
    public void feedHookActivityShowsWhenFabIsClickedWithUrl() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.onFabClicked("http://www.google.com");
        assertTrue(view.isFeedHookActivityForeground);
    }

    @Test
    public void feedHookActivityShowsWithUrlWhenFabIsClickedWithUrl() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        String url = "http://www.google.com";
        presenter.onFabClicked(url);
        assertThat(view.feedHookUrl, is(url));
    }

    @Test
    public void progressDialogShowsWhenHandleUrl() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle("http://www.google.com");
        assertTrue(view.isProgressDialogForeground);
    }

    @Test
    public void urlIsNotLoadedInWebViewWhenHandleUrl() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle("http://www.google.com");
        assertNull(view.loadedUrl);
    }

    @Test
    public void googleSearchIsExecutedInWebViewWhenHandleNotUrl() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle("abc");
        String url = "https://www.google.co.jp/search?q=abc";
        assertThat(view.loadedUrl, is(url));
    }

    @Test
    public void googleSearchIsExecutedInWebViewWhenHandleNotUrlJapanese() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle("あいうえお");
        String url = "https://www.google.co.jp/search?q=%E3%81%82%E3%81%84%E3%81%86%E3%81%88%E3%81%8A";
        assertThat(view.loadedUrl, is(url));
    }

    @Test
    public void googleSearchIsExecutedInWebViewWhenHandleEmpty() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle("");
        String url = "https://www.google.co.jp/search?q=";
        assertThat(view.loadedUrl, is(url));
    }

    @Test
    public void receiverIsNotRegisteredWhenHandleNotUrl() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle("abc");
        assertFalse(view.isReceiverRegistered);
    }

    @Test
    public void progressDialogDoesNotShowWhenHandleNotUrl() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle("abc");
        assertFalse(view.isProgressDialogForeground);
    }

    @Test
    public void successToastShowsWhenNewFeedIsAdded() {
        // Mock test feed returns
        String testUrl = "http://www.google.com";
        Feed testFeed = new Feed(1, "hoge", testUrl, "", "", 0, "");
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        Mockito.when(adapter.getFeedByUrl(testUrl)).thenReturn(testFeed);
        FeedSearchPresenter presenter = new FeedSearchPresenter(
                networkTaskManager, adapter, unreadCountManager, parser);

        MockView view = new MockView();
        presenter.setView(view);
        presenter.callback.succeeded(testUrl);
        assertTrue(view.isSuccessToastShowed);
    }

    @Test
    public void progressDialogDismissesWhenNewFeedIsAdded() {
        // Mock test feed returns
        String testUrl = "http://www.google.com";
        Feed testFeed = new Feed(1, "hoge", testUrl, "", "", 0, "");
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        Mockito.when(adapter.getFeedByUrl(testUrl)).thenReturn(testFeed);
        FeedSearchPresenter presenter = new FeedSearchPresenter(
                networkTaskManager, adapter, unreadCountManager, parser);

        FeedSearchView view = Mockito.mock(FeedSearchView.class);
        presenter.setView(view);
        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.NOT_FAILED);
        verify(view, times(1)).dismissProgressBar();
    }

    @Test
    public void viewFinishesWhenNewFeedIsAdded() {
        // Mock test feed returns
        String testUrl = "http://www.google.com";
        Feed testFeed = new Feed(1, "hoge", testUrl, "", "", 0, "");
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        Mockito.when(adapter.getFeedByUrl(testUrl)).thenReturn(testFeed);
        FeedSearchPresenter presenter = new FeedSearchPresenter(
                networkTaskManager, adapter, unreadCountManager, parser);

        FeedSearchView view = Mockito.mock(FeedSearchView.class);
        presenter.setView(view);
        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.NOT_FAILED);
        verify(view, times(1)).finishView();
    }

    @Test
    public void toastShowsWhenInvalidUrlComes() {
        FeedSearchView view = Mockito.mock(FeedSearchView.class);
        presenter.setView(view);
        String testUrl = "http://hogeagj.com";
        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.INVALID_URL);
        verify(view, times(1)).showInvalidUrlErrorToast();
    }

    @Test
    public void toastShowsWhenNotHtmlErrorOccurs() {
        FeedSearchView view = Mockito.mock(FeedSearchView.class);
        presenter.setView(view);
        String testUrl = "http://hogeagj.com";
        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.NON_RSS_HTML);
        verify(view, times(1)).showGenericErrorToast();
    }

    @Test
    public void toastShowsWhenNotFoundErrorOccurs() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        String testUrl = "http://hogeagj.com";
        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.NOT_FOUND);
        assertTrue(view.isGenericErrorToastShowed);
    }

    @Test
    public void toastShowsWhenNewFeedIsNotSaved() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        // Mock null returns
        String testUrl = "http://www.google.com";
        Mockito.when(adapter.getFeedByUrl(testUrl)).thenReturn(null);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedSearchPresenter presenter = new FeedSearchPresenter(
                networkTaskManager, adapter, unreadCountManager, parser);

        FeedSearchView view = Mockito.mock(FeedSearchView.class);
        presenter.setView(view);
        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.NOT_FOUND);
        verify(view, times(1)).showGenericErrorToast();
    }

    @Test
    public void WhenSearchGoogleSearchUrlIsSet() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.handle("hoge");
        assertEquals(view.searchViewUrl, "https://www.google.co.jp/search?q=hoge");
    }

    @Test
    public void failedUrlWillBeTracked() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        String testUrl = "http://hogeagj.com";
        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.INVALID_URL);
        assertThat(view.trackedUrl, is(testUrl));
    }

    private class MockView implements FeedSearchView {
        private boolean isReceiverRegistered = false;
        private boolean isFeedHookActivityForeground = false;
        private boolean isProgressDialogForeground = false;
        private boolean isSuccessToastShowed = false;
        private boolean isGenericErrorToastShowed = false;
        private String feedHookUrl;
        private String loadedUrl;
        private String searchViewUrl;
        private String trackedUrl;

        @Override
        public void startFeedUrlHookActivity(@NonNull String url) {
            isFeedHookActivityForeground = true;
            feedHookUrl = url;
        }

        @Override
        public void showProgressBar() {
            isProgressDialogForeground = true;
        }

        @Override
        public void dismissProgressBar() {
            isProgressDialogForeground = false;
        }

        @Override
        public void load(@NonNull String url) {
            loadedUrl = url;
        }

        @Override
        public void showInvalidUrlErrorToast() {
        }

        @Override
        public void showGenericErrorToast() {
            isGenericErrorToastShowed = true;
        }

        @Override
        public void showAddFeedSuccessToast() {
            isSuccessToastShowed = true;
        }

        @Override
        public void finishView() {
        }

        @Override
        public void setSearchViewTextFrom(@NonNull String url) {
            searchViewUrl = url;
        }

        @Override
        public void trackFailedUrl(@NonNull String url) {
            trackedUrl = url;
        }
    }
}
