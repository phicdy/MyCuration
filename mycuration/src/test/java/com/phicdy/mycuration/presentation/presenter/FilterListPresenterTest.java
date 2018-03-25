package com.phicdy.mycuration.presentation.presenter;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.data.filter.Filter;
import com.phicdy.mycuration.presentation.view.FilterListView;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FilterListPresenterTest {

    @Test
    public void testOnCreate() {
        // For coverage
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        FilterListPresenter presenter = new FilterListPresenter(adapter);
        presenter.setView(new MockView());
        presenter.create();
    }

    @Test
    public void listIsInitializedAfterOnResume() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        FilterListPresenter presenter = new FilterListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        assertTrue(view.isListInited);
    }

    @Test
    public void listItemSizeEqualsSizeInDatabaseAfterOnResume() {
        ArrayList<Filter> testFilters = new ArrayList<>();
        testFilters.add(new Filter(1, "test1", "testKeyword1", "http://test1.com", 1));
        testFilters.add(new Filter(2, "test2", "testKeyword2", "http://test2.com", 0));
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        Mockito.when(adapter.getAllFilters()).thenReturn(testFilters);
        FilterListPresenter presenter = new FilterListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        assertThat(view.filters.size(), is(testFilters.size()));
    }

    @Test
    public void testOnPause() {
        // For coverage
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        FilterListPresenter presenter = new FilterListPresenter(adapter);
        presenter.setView(new MockView());
        presenter.create();
        presenter.resume();
        presenter.pause();
    }

    @Test
    public void invalidPositionDeleteDoesNotDeleteFilter() {
        ArrayList<Filter> testFilters = new ArrayList<>();
        Filter testFilter1 = new Filter(1, "test1", "testKeyword1", "http://test1.com", 1);
        testFilters.add(testFilter1);
        testFilters.add(new Filter(2, "test2", "testKeyword2", "http://test2.com", 0));
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        Mockito.when(adapter.getAllFilters()).thenReturn(testFilters);
        FilterListPresenter presenter = new FilterListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.onDeleteMenuClicked(-1, testFilter1);
        assertThat(view.filters.size(), is(testFilters.size()));
    }

    @Test
    public void sizeIsDecreasedAfterDeleteFirstFilter() {
        ArrayList<Filter> testFilters = new ArrayList<>();
        Filter testFilter1 = new Filter(1, "test1", "testKeyword1", "http://test1.com", 1);
        Filter testFilter2 = new Filter(2, "test2", "testKeyword2", "http://test2.com", 0);
        testFilters.add(testFilter1);
        testFilters.add(testFilter2);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        Mockito.when(adapter.getAllFilters()).thenReturn(testFilters);
        FilterListPresenter presenter = new FilterListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.onDeleteMenuClicked(0, testFilter1);
        assertThat(view.filters.size(), is(1));
    }

    @Test
    public void firstItemIsSecondFilterAfterDeleteFirstFilter() {
        ArrayList<Filter> testFilters = new ArrayList<>();
        Filter testFilter1 = new Filter(1, "test1", "testKeyword1", "http://test1.com", 1);
        Filter testFilter2 = new Filter(2, "test2", "testKeyword2", "http://test2.com", 0);
        testFilters.add(testFilter1);
        testFilters.add(testFilter2);
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        Mockito.when(adapter.getAllFilters()).thenReturn(testFilters);
        FilterListPresenter presenter = new FilterListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.onDeleteMenuClicked(0, testFilter1);
        assertThat(view.filters.get(0).getTitle(), is("test2"));
    }

    @Test
    public void editActivityDoesNotStartWhenEditInvalidIdFilter() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        FilterListPresenter presenter = new FilterListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        Filter invalidFilter = new Filter(0, "test1", "testKeyword1", "http://test1.com", 1);
        presenter.onEditMenuClicked(invalidFilter);
        assertFalse(view.isStartEditActivity);
    }

    @Test
    public void editActivityStartsWhenEditValidIdFilter() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        FilterListPresenter presenter = new FilterListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        Filter validFilter = new Filter(1, "test1", "testKeyword1", "http://test1.com", 1);
        presenter.onEditMenuClicked(validFilter);
        assertTrue(view.isStartEditActivity);
    }

    @Test
    public void editIdEqualsClickedFilter() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        FilterListPresenter presenter = new FilterListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        Filter validFilter = new Filter(2, "test1", "testKeyword1", "http://test1.com", 1);
        presenter.onEditMenuClicked(validFilter);
        assertThat(view.startEditActivityId, is(2));
    }

    @Test
    public void filterIsEnabledWhenChecked() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        FilterListPresenter presenter = new FilterListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        Filter validFilter = new Filter(1, "test1", "testKeyword1", "http://test1.com", 1);
        presenter.onFilterCheckClicked(validFilter, true);
        assertTrue(validFilter.isEnabled());
    }

    @Test
    public void filterIsDisabledWhenUnchecked() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        FilterListPresenter presenter = new FilterListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        Filter validFilter = new Filter(1, "test1", "testKeyword1", "http://test1.com", 1);
        presenter.onFilterCheckClicked(validFilter, false);
        assertFalse(validFilter.isEnabled());
    }

    private class MockView implements FilterListView {

        private boolean isListInited = false;
        private boolean isStartEditActivity = false;
        private int startEditActivityId;
        private ArrayList<Filter> filters;

        @Override
        public void remove(int position) {
            filters.remove(position);
        }

        @Override
        public void notifyListChanged() {
        }

        @Override
        public void startEditActivity(int filterId) {
            isStartEditActivity = true;
            startEditActivityId = filterId;
        }

        @Override
        public void initList(@NonNull ArrayList<Filter> filters) {
            isListInited = true;
            this.filters = filters;
        }
    }
}
