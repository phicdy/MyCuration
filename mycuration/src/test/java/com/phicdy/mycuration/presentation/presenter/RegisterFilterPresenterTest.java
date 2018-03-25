package com.phicdy.mycuration.presentation.presenter;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.presentation.view.RegisterFilterView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class RegisterFilterPresenterTest {

    private DatabaseAdapter mockAdapter;
    private RegisterFilterPresenter presenter;
    private MockView mockView;

    @Before
    public void setup() {
        mockAdapter = Mockito.mock(DatabaseAdapter.class);
        presenter = new RegisterFilterPresenter(mockAdapter, 1);
        mockView = new MockView();
        presenter.setView(mockView);
    }

    @Test
    public void setOneSelectedFeedList() {
        ArrayList<Feed> testFeeds = new ArrayList<>();
        testFeeds.add(new Feed(1, "testFeed"));
        presenter.setSelectedFeedList(testFeeds);
        assertEquals(mockView.filterTarget, "testFeed");
    }

    @Test
    public void setMultipleSelectedFeedList() {
        ArrayList<Feed> testFeeds = new ArrayList<>();
        testFeeds.add(new Feed(1, "testFeed"));
        testFeeds.add(new Feed(2, "testFeed2"));
        presenter.setSelectedFeedList(testFeeds);
        assertEquals(mockView.filterTarget, MockView.MULTIPLE_FILTER_TARGET);
    }

    @Test
    public void setEmptySelectedFeedList() {
        ArrayList<Feed> testFeeds = new ArrayList<>();
        presenter.setSelectedFeedList(testFeeds);
        assertEquals(mockView.filterTarget, MockView.DEFAULT_FILTER_TARGET);
    }

    @Test
    public void setNullSelectedFeedList() {
        presenter.setSelectedFeedList(null);
        assertEquals(mockView.filterTarget, MockView.DEFAULT_FILTER_TARGET);
    }

    private class MockView implements RegisterFilterView {
        private String title;
        private String filterTarget = DEFAULT_FILTER_TARGET;
        private static final String DEFAULT_FILTER_TARGET = "default";
        private static final String MULTIPLE_FILTER_TARGET = "multiple-target";

        @Override
        public String filterKeyword() {
            return null;
        }

        @Override
        public String filterUrl() {
            return null;
        }

        @Override
        public String filterTitle() {
            return title;
        }

        @Override
        public void setFilterTitle(@NonNull String title) {
            this.title = title;
        }

        @Override
        public void setFilterTargetRss(@NonNull String rss) {
            this.filterTarget = rss;
        }

        @Override
        public void setMultipleFilterTargetRss() {
            this.filterTarget = MULTIPLE_FILTER_TARGET;
        }

        @Override
        public void resetFilterTargetRss() {
            this.filterTarget = DEFAULT_FILTER_TARGET;
        }

        @Override
        public void setFilterUrl(@NonNull String url) {

        }

        @Override
        public void setFilterKeyword(@NonNull String keyword) {

        }

        @Override
        public void handleEmptyTitle() {

        }

        @Override
        public void handleEmptyCondition() {

        }

        @Override
        public void handlePercentOnly() {

        }

        @Override
        public void finish() {

        }

        @Override
        public void showSaveSuccessToast() {

        }

        @Override
        public void showSaveErrorToast() {

        }

        @Override
        public void trackEdit() {

        }

        @Override
        public void trackRegister() {

        }
    }
}