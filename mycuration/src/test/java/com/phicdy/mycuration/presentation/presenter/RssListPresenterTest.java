package com.phicdy.mycuration.presentation.presenter;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.domain.rss.UnreadCountManager;
import com.phicdy.mycuration.domain.task.NetworkTaskManager;
import com.phicdy.mycuration.presentation.view.RssListView;
import com.phicdy.mycuration.presentation.view.fragment.RssListFragment;
import com.phicdy.mycuration.util.PreferenceHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RssListPresenterTest {

    private RssListPresenter presenter;
    private MockView view;

    private static final String FIRST_RSS_TITLE = "rss1";
    private static final String SECOND_RSS_TITLE = "rss2";
    private static final int FIRST_RSS_ID = 0;
    private static final int SECOND_RSS_ID = 1;

    private static final int FIRST_RSS_POSITION = 0;
    private static final int HIDE_OPTION_POSITION_WHEN_HIDDEN = 1;

    @Before
    public void setup() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        DatabaseAdapter.inject(adapter);
        ArrayList<Feed> allFeeds = new ArrayList<>();
        Feed firstRss = new Feed(FIRST_RSS_ID, FIRST_RSS_TITLE, "", "", "", 0, "");
        Feed secondRss = new Feed(SECOND_RSS_ID, SECOND_RSS_TITLE, "", "", "", 1, "");
        allFeeds.add(firstRss);
        allFeeds.add(secondRss);
        Mockito.when(adapter.getAllFeedsWithNumOfUnreadArticles()).thenReturn(allFeeds);
        NetworkTaskManager networkTaskManager = NetworkTaskManager.INSTANCE;

        Context mockContext = Mockito.mock(Context.class);
        Mockito.when(mockContext.getSharedPreferences("FilterPref", Context.MODE_PRIVATE))
                .thenReturn(Mockito.mock(SharedPreferences.class));
        PreferenceHelper.INSTANCE.setUp(mockContext);
        PreferenceHelper preferenceHelper = PreferenceHelper.INSTANCE;

        UnreadCountManager.INSTANCE.addFeed(firstRss);
        UnreadCountManager.INSTANCE.addFeed(secondRss);
        view = new MockView();
        presenter = new RssListPresenter(view, preferenceHelper, adapter, networkTaskManager, UnreadCountManager.INSTANCE);
    }

    @After
    public void tearDown() {
        UnreadCountManager.INSTANCE.clear();
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
        presenter.onFeedListClicked(HIDE_OPTION_POSITION_WHEN_HIDDEN, new RssListFragment.OnFeedListFragmentListener() {
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

    private class MockView implements RssListView {

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