package com.phicdy.mycuration.presentation.presenter;

import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.data.filter.Filter;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.presentation.view.FilterListView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FilterListPresenterTest {

    private DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
    private FilterListPresenter presenter;
    private FilterListView view;

    @Before
    public void setup() {
        view = Mockito.mock(FilterListView.class);
        presenter = new FilterListPresenter(view, adapter);
    }

    @Test
    public void testOnCreate() {
        // For coverage
        presenter.create();
    }

    @Test
    public void testOnResume() {
        // For coverage
        presenter.resume();
    }

    @Test
    public void testOnPause() {
        // For coverage
        presenter.pause();
    }

    @Test
    public void invalidPositionDeleteDoesNotDeleteFilter() {
        Filter testFilter1 = new Filter(1, "test1", "testKeyword1", "http://test1.com", new ArrayList<Feed>(), -1, Filter.TRUE);
        presenter.onDeleteMenuClicked(-1, testFilter1);
        verify(view, times(0)).remove(-1);
    }

    @Test
    public void sizeIsDecreasedAfterDeleteFirstFilter() {
        Filter testFilter1 = new Filter(1, "test1", "testKeyword1", "http://test1.com", new ArrayList<Feed>(), -1, Filter.TRUE);
        presenter.onDeleteMenuClicked(0, testFilter1);
        verify(view, times(1)).remove(0);
    }

    @Test
    public void editActivityDoesNotStartWhenEditInvalidIdFilter() {
        Filter invalidFilter = new Filter(0, "test1", "testKeyword1", "http://test1.com", new ArrayList<Feed>(), -1, Filter.TRUE);
        presenter.onEditMenuClicked(invalidFilter);
        verify(view, times(0)).startEditActivity(invalidFilter.getId());
    }

    @Test
    public void editActivityStartsWhenEditValidIdFilter() {
        Filter validFilter = new Filter(1, "test1", "testKeyword1", "http://test1.com", new ArrayList<Feed>(), -1, Filter.TRUE);
        presenter.onEditMenuClicked(validFilter);
        verify(view, times(1)).startEditActivity(validFilter.getId());
    }

    @Test
    public void filterIsEnabledWhenChecked() {
        Filter validFilter = new Filter(1, "test1", "testKeyword1", "http://test1.com", new ArrayList<Feed>(), -1, Filter.TRUE);
        presenter.onFilterCheckClicked(validFilter, true);
        assertTrue(validFilter.isEnabled());
    }

    @Test
    public void filterIsDisabledWhenUnchecked() {
        Filter validFilter = new Filter(1, "test1", "testKeyword1", "http://test1.com", new ArrayList<Feed>(), -1, Filter.TRUE);
        presenter.onFilterCheckClicked(validFilter, false);
        assertFalse(validFilter.isEnabled());
    }
}
