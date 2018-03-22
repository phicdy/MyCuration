package com.phicdy.mycuration.presentation.presenter;

import android.content.Intent;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.data.rss.RssParseResult;
import com.phicdy.mycuration.data.rss.RssParser;
import com.phicdy.mycuration.data.rss.UnreadCountManager;
import com.phicdy.mycuration.domain.task.NetworkTaskManager;
import com.phicdy.mycuration.presentation.view.FeedUrlHookView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertTrue;

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
        presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager, parser);

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
    public void finishWhenInvalidActionComes() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle("hogehoge", "http://www.google.com");
        assertTrue(view.isFinished);
    }

    @Test
    public void finishWhenEmptyActionComes() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle("", "http://www.google.com");
        assertTrue(view.isFinished);
    }

    @Test
    public void toastShowsWhenActionViewAndInvalidUrlComes() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle(Intent.ACTION_VIEW, "hogehoge");
        assertTrue(view.isInvalidUrlErrorToastShowed);
    }

    @Test
    public void toastShowsWhenActionSendAndInvalidUrlComes() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle(Intent.ACTION_SEND, "hogehoge");
        assertTrue(view.isInvalidUrlErrorToastShowed);
    }

    @Test
    public void viewFinishesWhenFinishAddFeedActionComes() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.callback.failed(RssParseResult.NOT_FOUND);
        assertTrue(view.isFinished);
    }

    @Test
    public void toastShowsWhenFinishAddFeedActionComesWithNotRssHtmlError() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.callback.failed(RssParseResult.NON_RSS_HTML);
        assertTrue(view.isGenericErrorToastShowed);
    }

    @Test
    public void toastShowsWhenFinishAddFeedActionComesWithInvalidUrlError() {
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.callback.failed(RssParseResult.INVALID_URL);
        assertTrue(view.isInvalidUrlErrorToastShowed);
    }

    private class MockView implements FeedUrlHookView {
        private boolean isSuccessToastShowed = false;
        private boolean isInvalidUrlErrorToastShowed = false;
        private boolean isGenericErrorToastShowed = false;
        private boolean isFinished = false;

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
    }
}
