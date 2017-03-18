package com.phicdy.mycuration.presenter;

import android.content.Intent;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.phicdy.mycuration.task.NetworkTaskManager;
import com.phicdy.mycuration.view.FeedUrlHookView;

import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class FeedUrlHookPresenterTest {

    @Test
    public void testOnCreate() {
        // For coverage
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        presenter.setView(new MockView());
        presenter.create();
        assertTrue(true);
    }

    @Test
    public void testOnResume() {
        // For coverage
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        presenter.setView(new MockView());
        presenter.create();
        presenter.resume();
        assertTrue(true);
    }

    @Test
    public void receiverIsUnregisteredInAfterPause() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.pause();
        assertFalse(view.isReceiverRegistered);
    }

    @Test
    public void finishWhenInvalidActionComes() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle("hogehoge", "http://www.google.com");
        assertTrue(view.isFinished);
    }

    @Test
    public void finishWhenEmptyActionComes() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle("", "http://www.google.com");
        assertTrue(view.isFinished);
    }

    @Test
    public void registerReceiverWhenActionViewAndUrlComes() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle(Intent.ACTION_VIEW, "http://www.google.com");
        assertTrue(view.isReceiverRegistered);
    }

    @Test
    public void progressDialogShowsWhenActionViewAndUrlComes() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle(Intent.ACTION_VIEW, "http://www.google.com");
        assertTrue(view.isProgressDialogForeground);
    }

    @Test
    public void toastShowsWhenActionViewAndInvalidUrlComes() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle(Intent.ACTION_VIEW, "hogehoge");
        assertTrue(view.isInvalidUrlErrorToastShowed);
    }

    @Test
    public void registerReceiverWhenActionSendAndUrlComes() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle(Intent.ACTION_SEND, "http://www.google.com");
        assertTrue(view.isReceiverRegistered);
    }

    @Test
    public void progressDialogShowsWhenActionSendAndUrlComes() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle(Intent.ACTION_SEND, "http://www.google.com");
        assertTrue(view.isProgressDialogForeground);
    }

    @Test
    public void toastShowsWhenActionSendAndInvalidUrlComes() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handle(Intent.ACTION_SEND, "hogehoge");
        assertTrue(view.isInvalidUrlErrorToastShowed);
    }

    @Test
    public void viewDoesNotFinishWhenInvalidActionComes() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handleFinish("hogehoge", "http://www.google.com", NetworkTaskManager.REASON_NOT_FOUND);
        assertFalse(view.isFinished);
    }

    @Test
    public void viewFinishesWhenFinishAddFeedActionComes() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handleFinish(NetworkTaskManager.FINISH_ADD_FEED,
                "http://www.google.com", NetworkTaskManager.REASON_NOT_FOUND);
        assertTrue(view.isFinished);
    }

    @Test
    public void progressDialogDismissesWhenFinishAddFeedActionComes() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handleFinish(NetworkTaskManager.FINISH_ADD_FEED,
                "http://www.google.com", NetworkTaskManager.REASON_NOT_FOUND);
        assertFalse(view.isProgressDialogForeground);
    }

    @Test
    public void toastShowsWhenFinishAddFeedActionComes() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        String testUrl = "http://www.google.com";
        Feed testFeed = new Feed(1, "Google");
        Mockito.when(adapter.getFeedByUrl(testUrl)).thenReturn(testFeed);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handleFinish(NetworkTaskManager.FINISH_ADD_FEED,
                testUrl, NetworkTaskManager.REASON_NOT_FOUND);
        assertTrue(view.isSuccessToastShowed);
    }

    @Test
    public void toastShowsWhenFinishAddFeedActionComesWithNotRssHtmlError() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handleFinish(NetworkTaskManager.FINISH_ADD_FEED,
                "http://www.google.com", NetworkTaskManager.ERROR_NON_RSS_HTML_CONTENT);
        assertTrue(view.isGenericErrorToastShowed);
    }

    @Test
    public void toastShowsWhenFinishAddFeedActionComesWithUnknownError() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handleFinish(NetworkTaskManager.FINISH_ADD_FEED,
                "http://www.google.com", NetworkTaskManager.ERROR_UNKNOWN);
        assertTrue(view.isGenericErrorToastShowed);
    }

    @Test
    public void toastShowsWhenFinishAddFeedActionComesWithInvalidUrlError() {
        NetworkTaskManager networkTaskManager = Mockito.mock(NetworkTaskManager.class);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        UnreadCountManager unreadCountManager = Mockito.mock(UnreadCountManager.class);
        FeedUrlHookPresenter presenter = new FeedUrlHookPresenter(
                adapter, unreadCountManager, networkTaskManager);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handleFinish(NetworkTaskManager.FINISH_ADD_FEED,
                "http://www.google.com", NetworkTaskManager.ERROR_INVALID_URL);
        assertTrue(view.isInvalidUrlErrorToastShowed);
    }

    private class MockView implements FeedUrlHookView {
        private boolean isReceiverRegistered = false;
        private boolean isProgressDialogForeground = false;
        private boolean isSuccessToastShowed = false;
        private boolean isInvalidUrlErrorToastShowed = false;
        private boolean isGenericErrorToastShowed = false;
        private boolean isFinished = false;

        @Override
        public void showProgressDialog() {
            isProgressDialogForeground = true;
        }

        @Override
        public void dismissProgressDialog() {
            isProgressDialogForeground = false;
        }

        @Override
        public void registerFinishAddReceiver() {
            isReceiverRegistered = true;
        }

        @Override
        public void unregisterFinishAddReceiver() {
            isReceiverRegistered = false;
        }

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
