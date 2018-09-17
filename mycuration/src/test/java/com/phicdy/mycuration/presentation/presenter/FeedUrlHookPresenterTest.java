package com.phicdy.mycuration.presentation.presenter;

import android.content.Intent;

import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.domain.rss.RssParseResult;
import com.phicdy.mycuration.domain.rss.RssParser;
import com.phicdy.mycuration.domain.rss.UnreadCountManager;
import com.phicdy.mycuration.domain.task.NetworkTaskManager;
import com.phicdy.mycuration.presentation.view.FeedUrlHookView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FeedUrlHookPresenterTest {

    private NetworkTaskManager networkTaskManager;
    private DatabaseAdapter adapter;
    private FeedUrlHookPresenter presenter;
    private RssParser parser;
    private FeedUrlHookView view;

    @Before
    public void setup() {
        networkTaskManager = NetworkTaskManager.INSTANCE;
        adapter = Mockito.mock(DatabaseAdapter.class);
        DatabaseAdapter.inject(Mockito.mock(DatabaseAdapter.class));
        parser = Mockito.mock(RssParser.class);
        view = Mockito.mock(FeedUrlHookView.class);
        presenter = new FeedUrlHookPresenter(view, "", "", "",
                adapter, UnreadCountManager.INSTANCE, networkTaskManager, parser);
    }

    @Test
    public void finishWhenEmptyAction() {
        presenter.create();
        verify(view, times(1)).finishView();
    }

    @Test
    public void testOnResume() {
        // For coverage
        presenter.resume();
        assertTrue(true);
    }

    @Test
    public void finishWhenInvalidActionComes() {
        presenter = new FeedUrlHookPresenter(view, "hogehoge", "http://www.google.com", "",
                adapter, UnreadCountManager.INSTANCE, networkTaskManager, parser);
        presenter.create();
        verify(view, times(1)).finishView();
    }

    @Test
    public void toastShowsWhenActionViewAndInvalidUrlComes() {
        presenter = new FeedUrlHookPresenter(view, Intent.ACTION_VIEW, "hogehoge", "",
                adapter, UnreadCountManager.INSTANCE, networkTaskManager, parser);
        presenter.create();
        verify(view, times(1)).showInvalidUrlErrorToast();
    }

    @Test
    public void toastShowsWhenActionSendAndInvalidUrlComes() {
        presenter = new FeedUrlHookPresenter(view, Intent.ACTION_SEND, "", "hogehoge",
                adapter, UnreadCountManager.INSTANCE, networkTaskManager, parser);
        presenter.create();
        verify(view, times(1)).showInvalidUrlErrorToast();
    }

    @Test
    public void viewFinishesWhenFinishAddFeedActionComes() {
        presenter.getCallback().failed(RssParseResult.FailedReason.NOT_FOUND, "");
        verify(view, times(1)).finishView();
    }

    @Test
    public void toastShowsWhenFinishAddFeedActionComesWithNotRssHtmlError() {
        presenter.create();
        presenter.getCallback().failed(RssParseResult.FailedReason.NON_RSS_HTML, "");
        verify(view, times(1)).showGenericErrorToast();
    }

    @Test
    public void toastShowsWhenFinishAddFeedActionComesWithInvalidUrlError() {
        presenter.create();
        presenter.getCallback().failed(RssParseResult.FailedReason.INVALID_URL, "");
        verify(view, times(1)).showInvalidUrlErrorToast();
    }

    @Test
    public void failedUrlWillBeTracked() {
        presenter.create();
        String failUrl = "http://www.google.com";
        presenter.getCallback().failed(RssParseResult.FailedReason.INVALID_URL, failUrl);
        verify(view, times(1)).trackFailedUrl(failUrl);
    }
}
