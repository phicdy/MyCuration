package com.phicdy.mycuration.presentation.presenter;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.domain.rss.UnreadCountManager;
import com.phicdy.mycuration.domain.task.NetworkTaskManager;
import com.phicdy.mycuration.util.PreferenceHelper;
import com.phicdy.mycuration.presentation.view.FeedListView;
import com.phicdy.mycuration.presentation.view.fragment.FeedListFragment;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FeedListPresenterTest {

    private FeedListPresenter presenter;
    private DatabaseAdapter adapter;
    private NetworkTaskManager networkTaskManager;
    private UnreadCountManager unreadCountManager;
    private PreferenceHelper preferenceHelper;
    private MockView view;

    private static final String FIRST_RSS_TITLE = "rss1";
    private static final String SECOND_RSS_TITLE = "rss2";
    private static final int FIRST_RSS_ID = 0;
    private static final int SECOND_RSS_ID = 1;

    private static final int FIRST_RSS_POSITION = 0;
    private static final int SECOND_RSS_POSITION = 1;
    private static final int HIDE_OPTION_POSITION_WHEN_HIDDEN = 1;

    @Before
    public void setup() {
        adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Feed> allFeeds = new ArrayList<>();
        allFeeds.add(new Feed(FIRST_RSS_ID, FIRST_RSS_TITLE, "", "", "", 0));
        allFeeds.add(new Feed(SECOND_RSS_ID, SECOND_RSS_TITLE, "", "", "", 1));
        Mockito.when(adapter.getAllFeedsWithNumOfUnreadArticles()).thenReturn(allFeeds);
        networkTaskManager = NetworkTaskManager.INSTANCE;
        unreadCountManager = Mockito.mock(UnreadCountManager.class);

        Context mockContext = Mockito.mock(Context.class);
        Mockito.when(mockContext.getSharedPreferences("FilterPref", Context.MODE_PRIVATE))
                .thenReturn(Mockito.mock(SharedPreferences.class));
        PreferenceHelper.INSTANCE.setUp(mockContext);
        preferenceHelper = PreferenceHelper.INSTANCE;

        Mockito.when(unreadCountManager.getUnreadCount(FIRST_RSS_ID))
                .thenReturn(0);
        Mockito.when(unreadCountManager.getUnreadCount(SECOND_RSS_ID))
                .thenReturn(1);
        Mockito.when(unreadCountManager.getUnreadCount(Feed.DEFAULT_FEED_ID)).thenReturn(-1);
        presenter = new FeedListPresenter(preferenceHelper, adapter, networkTaskManager, unreadCountManager);
        view = new MockView();
        presenter.setView(view);
    }

    @Test
    public void WhenFirstRssIsHiddenThenFirstEditTitleWillBeSecondRss() {
        // Default hidden option is enaled
        presenter.create();
        presenter.resume();
        presenter.onEditFeedMenuClicked(FIRST_RSS_POSITION);
        assertThat(view.editTitle, is(SECOND_RSS_TITLE));
    }

    @Test
    public void WhenRssIsNotHiddenThenFirstEditTitleWillBeFirstRss() {
        presenter.create();
        presenter.resume();
        // Disale hidden option
        presenter.onFeedListClicked(HIDE_OPTION_POSITION_WHEN_HIDDEN, new FeedListFragment.OnFeedListFragmentListener() {
            @Override
            public void onListClicked(int feedId) {
            }

            @Override
            public void onAllUnreadClicked() {
            }
        });
        presenter.onEditFeedMenuClicked(FIRST_RSS_POSITION);
        assertThat(view.editTitle, is(FIRST_RSS_TITLE));
    }

    private class MockView implements FeedListView {

        private String editTitle;

        @Override
        public void showDeleteFeedAlertDialog(int position) {

        }

        @Override
        public void showEditTitleDialog(int position, @NonNull String feedTitle) {
            this.editTitle = feedTitle;
        }

        @Override
        public void setRefreshing(boolean doScroll) {

        }

        @Override
        public void init(@NonNull ArrayList<Feed> feeds) {

        }

        @Override
        public void setTotalUnreadCount(int count) {

        }

        @Override
        public void onRefreshCompleted() {

        }

        @Override
        public void showEditFeedTitleEmptyErrorToast() {

        }

        @Override
        public void showEditFeedFailToast() {

        }

        @Override
        public void showEditFeedSuccessToast() {

        }

        @Override
        public void showDeleteSuccessToast() {

        }

        @Override
        public void showDeleteFailToast() {

        }

        @Override
        public void showAddFeedSuccessToast() {

        }

        @Override
        public void showGenericAddFeedErrorToast() {

        }

        @Override
        public void showInvalidUrlAddFeedErrorToast() {

        }

        @Override
        public void notifyDataSetChanged() {

        }

        @Override
        public void showAllUnreadView() {

        }

        @Override
        public void hideAllUnreadView() {

        }
    }
}