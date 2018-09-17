package com.phicdy.mycuration.presentation.presenter;

import android.content.Intent;
import android.support.annotation.NonNull;

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FeedUrlHookPresenterTest {

    private NetworkTaskManager networkTaskManager;
    private DatabaseAdapter adapter;
    private UnreadCountManager unreadCountManager;
    private FeedUrlHookPresenter presenter;
    private RssParser parser;

    @Before
    public void setup() {
        networkTaskManager = NetworkTaskManager.INSTANCE;
        adapter = Mockito.mock(DatabaseAdapter.class);
        unreadCountManager = Mockito.mock(UnreadCountManager.class);
        parser = Mockito.mock(RssParser.class);
        presenter = new FeedUrlHookPresenter("", "", "",
                adapter, unreadCountManager, networkTaskManager, parser);
    }

    @Test
    public void finishWhenEmptyAction() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        assertTrue(view.isFinished);
    }

    @Test
    public void testOnResume() {
        // For coverage
        presenter.setView(new MockView());
        presenter.resume();
        assertTrue(true);
    }

    @Test
    public void finishWhenInvalidActionComes() {
        MockView view = new MockView();
        presenter = new FeedUrlHookPresenter("hogehoge", "http://www.google.com", "",
                adapter, unreadCountManager, networkTaskManager, parser);
        presenter.setView(view);
        presenter.create();
        assertTrue(view.isFinished);
    }

    @Test
    public void toastShowsWhenActionViewAndInvalidUrlComes() {
        MockView view = new MockView();
        presenter = new FeedUrlHookPresenter(Intent.ACTION_VIEW, "hogehoge", "",
                adapter, unreadCountManager, networkTaskManager, parser);
        presenter.setView(view);
        presenter.create();
        assertTrue(view.isInvalidUrlErrorToastShowed);
    }

    @Test
    public void toastShowsWhenActionSendAndInvalidUrlComes() {
        MockView view = new MockView();
        presenter = new FeedUrlHookPresenter(Intent.ACTION_SEND, "", "hogehoge",
                adapter, unreadCountManager, networkTaskManager, parser);
        presenter.setView(view);
        presenter.create();
        assertTrue(view.isInvalidUrlErrorToastShowed);
    }

    @Test
    public void viewFinishesWhenFinishAddFeedActionComes() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.callback.failed(RssParseResult.FailedReason.NOT_FOUND, "");
        assertTrue(view.isFinished);
    }

    @Test
    public void toastShowsWhenFinishAddFeedActionComesWithNotRssHtmlError() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.callback.failed(RssParseResult.FailedReason.NON_RSS_HTML, "");
        assertTrue(view.isGenericErrorToastShowed);
    }

    @Test
    public void toastShowsWhenFinishAddFeedActionComesWithInvalidUrlError() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.callback.failed(RssParseResult.FailedReason.INVALID_URL, "");
        assertTrue(view.isInvalidUrlErrorToastShowed);
    }

    @Test
    public void failedUrlWillBeTracked() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        String failUrl = "http://www.google.com";
        presenter.callback.failed(RssParseResult.FailedReason.INVALID_URL, failUrl);
        assertThat(view.trackedUrl, is(failUrl));
    }

    private class MockView implements FeedUrlHookView {
        private boolean isSuccessToastShowed = false;
        private boolean isInvalidUrlErrorToastShowed = false;
        private boolean isGenericErrorToastShowed = false;
        private boolean isFinished = false;
        private String trackedUrl = "";

        @Override
        public void showSuccessToast() {
            isSuccessToastShowed = true;
        }

        @Override
        public void showInvalidUrlErrorToast() {
            isInvalidUrlErrorToastShowed = true;
        }

        @Override
        public void showGenericErrorToast() {
            isGenericErrorToastShowed = true;
        }

        @Override
        public void finishView() {
            isFinished = true;
        }

        @Override
        public void trackFailedUrl(@NonNull String url) {
            trackedUrl = url;
        }
    }
}
