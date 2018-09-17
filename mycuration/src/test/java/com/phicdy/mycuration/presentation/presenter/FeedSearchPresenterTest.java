package com.phicdy.mycuration.presentation.presenter;

import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.domain.rss.RssParseResult;
import com.phicdy.mycuration.domain.rss.RssParser;
import com.phicdy.mycuration.domain.rss.UnreadCountManager;
import com.phicdy.mycuration.domain.task.NetworkTaskManager;
import com.phicdy.mycuration.presentation.view.FeedSearchView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FeedSearchPresenterTest {

    private NetworkTaskManager networkTaskManager;
    private RssParser parser;
    private FeedSearchPresenter presenter;
    private FeedSearchView view;

    @Before
    public void setup() {
        networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        DatabaseAdapter.inject(adapter);
        parser = Mockito.mock(RssParser.class);
        view = Mockito.mock(FeedSearchView.class);
        presenter = new FeedSearchPresenter(view,
                networkTaskManager, adapter, UnreadCountManager.INSTANCE, parser);

    }

    @Test
    public void testOnCreate() {
        // For coverage
        presenter.create();
        assertTrue(true);
    }

    @Test
    public void testOnResume() {
        // For coverage
        presenter.resume();
        assertTrue(true);
    }

    @Test
    public void feedHookActivityDoesNotShowWhenFabIsClickedWithEmpty() {
        presenter.onFabClicked("");
        verify(view, times(0)).startFeedUrlHookActivity("");
    }

    @Test
    public void feedHookActivityShowsWhenFabIsClickedWithUrl() {
        presenter.onFabClicked("http://www.google.com");
        verify(view, times(1)).startFeedUrlHookActivity("http://www.google.com");
    }

    @Test
    public void progressDialogShowsWhenHandleUrl() {
        presenter.handle("http://www.google.com");
        verify(view, times(1)).showProgressBar();
    }

    @Test
    public void urlIsNotLoadedInWebViewWhenHandleUrl() {
        presenter.handle("http://www.google.com");
        verify(view, times(0)).load("http://www.google.com");
    }

    @Test
    public void googleSearchIsExecutedInWebViewWhenHandleNotUrl() {
        presenter.handle("abc");
        String url = "https://www.google.co.jp/search?q=abc";
        verify(view, times(1)).load(url);
    }

    @Test
    public void googleSearchIsExecutedInWebViewWhenHandleNotUrlJapanese() {
        presenter.handle("あいうえお");
        String url = "https://www.google.co.jp/search?q=%E3%81%82%E3%81%84%E3%81%86%E3%81%88%E3%81%8A";
        verify(view, times(1)).load(url);
    }

    @Test
    public void googleSearchIsExecutedInWebViewWhenHandleEmpty() {
        presenter.handle("");
        String url = "https://www.google.co.jp/search?q=";
        verify(view, times(1)).load(url);
    }

    @Test
    public void progressDialogDoesNotShowWhenHandleNotUrl() {
        presenter.handle("abc");
        verify(view, times(0)).showProgressBar();
    }

    @Test
    public void successToastShowsWhenNewFeedIsAdded() {
        // Mock test feed returns
        String testUrl = "http://www.google.com";
        Feed testFeed = new Feed(1, "hoge", testUrl, "", "", 0, "");
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        Mockito.when(adapter.getFeedByUrl(testUrl)).thenReturn(testFeed);
        FeedSearchPresenter presenter = new FeedSearchPresenter(view,
                networkTaskManager, adapter, UnreadCountManager.INSTANCE, parser);

        presenter.getCallback().succeeded(testUrl);
        verify(view, times(1)).showAddFeedSuccessToast();
    }

    @Test
    public void progressDialogDismissesWhenNewFeedIsAdded() {
        // Mock test feed returns
        String testUrl = "http://www.google.com";
        Feed testFeed = new Feed(1, "hoge", testUrl, "", "", 0, "");
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        Mockito.when(adapter.getFeedByUrl(testUrl)).thenReturn(testFeed);
        FeedSearchView view = Mockito.mock(FeedSearchView.class);
        FeedSearchPresenter presenter = new FeedSearchPresenter(view,
                networkTaskManager, adapter, UnreadCountManager.INSTANCE, parser);

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
        FeedSearchPresenter presenter = new FeedSearchPresenter(view,
                networkTaskManager, adapter, UnreadCountManager.INSTANCE, parser);

        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.NOT_FAILED);
        verify(view, times(1)).finishView();
    }

    @Test
    public void toastShowsWhenInvalidUrlComes() {
        String testUrl = "http://hogeagj.com";
        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.INVALID_URL);
        verify(view, times(1)).showInvalidUrlErrorToast();
    }

    @Test
    public void toastShowsWhenNotHtmlErrorOccurs() {
        String testUrl = "http://hogeagj.com";
        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.NON_RSS_HTML);
        verify(view, times(1)).showGenericErrorToast();
    }

    @Test
    public void toastShowsWhenNotFoundErrorOccurs() {
        presenter.create();
        presenter.resume();
        String testUrl = "http://hogeagj.com";
        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.NOT_FOUND);
        verify(view, times(1)).showGenericErrorToast();
    }

    @Test
    public void toastShowsWhenNewFeedIsNotSaved() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        // Mock null returns
        String testUrl = "http://www.google.com";
        Mockito.when(adapter.getFeedByUrl(testUrl)).thenReturn(null);
        FeedSearchPresenter presenter = new FeedSearchPresenter(view,
                networkTaskManager, adapter, UnreadCountManager.INSTANCE, parser);

        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.NOT_FOUND);
        verify(view, times(1)).showGenericErrorToast();
    }

    @Test
    public void WhenSearchGoogleSearchUrlIsSet() {
        presenter.handle("hoge");
        verify(view, times(1)).setSearchViewTextFrom("https://www.google.co.jp/search?q=hoge");
    }

    @Test
    public void failedUrlWillBeTracked() {
        String testUrl = "http://hogeagj.com";
        presenter.onFinishAddFeed(testUrl, RssParseResult.FailedReason.INVALID_URL);
        verify(view, times(1)).trackFailedUrl(testUrl);
    }
}
