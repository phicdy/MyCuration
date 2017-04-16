package com.phicdy.mycuration.presenter;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Curation;
import com.phicdy.mycuration.view.CurationListView;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class CurationListPresenterTest {

    @Test
    public void testOnCreate() {
        // For coverage
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        CurationListPresenter presenter = new CurationListPresenter(adapter);
        presenter.setView(new MockView());
        presenter.create();
    }

    @Test
    public void listIsSetAfterOnResume() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Curation> curations = new ArrayList<>();
        String testName = "testCuration";
        curations.add(new Curation(1, testName));
        when(adapter.getAllCurations()).thenReturn(curations);
        CurationListPresenter presenter = new CurationListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        assertThat(view.curations.get(0).getName(), is(testName));
    }

    @Test
    public void testOnPause() {
        // For coverage
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        CurationListPresenter presenter = new CurationListPresenter(adapter);
        presenter.setView(new MockView());
        presenter.create();
        presenter.resume();
        presenter.pause();
    }

    @Test
    public void EditActivityStartsWhenEditIsClicked() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        CurationListPresenter presenter = new CurationListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.onCurationEditClicked(1);
        assertThat(view.startedEditCurationId, is(1));
    }

    @Test
    public void InvalidCurationDoesNotAffectWhenEditIsClicked() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        CurationListPresenter presenter = new CurationListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.onCurationEditClicked(-1);
        assertThat(view.startedEditCurationId, is(MockView.DEFAULT_EDIT_ID));
    }

    @Test
    public void EmptyViewIsSetIfEmptyRss() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        when(adapter.getNumOfFeeds()).thenReturn(0);
        CurationListPresenter presenter = new CurationListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.activityCreated();
        assertThat(view.startedEditCurationId, is(MockView.DEFAULT_EDIT_ID));
    }

    @Test
    public void CurationIsDeletedWhenDeleteIsClicked() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Curation> curations = new ArrayList<>();
        Curation curation = new Curation(1, "test");
        curations.add(curation);
        when(adapter.getAllCurations()).thenReturn(curations);
        CurationListPresenter presenter = new CurationListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.onCurationDeleteClicked(curation);
        assertThat(view.curations.size(), is(0));
    }

    @Test
    public void NoRssViewIsSetWhenRssIsEmpty() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        when(adapter.getNumOfFeeds()).thenReturn(0);
        CurationListPresenter presenter = new CurationListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.activityCreated();
        assertTrue(view.isNoRssViewSet);
    }

    @Test
    public void EmptyViewIsSetWhenRssIsEmpty() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        when(adapter.getNumOfFeeds()).thenReturn(0);
        CurationListPresenter presenter = new CurationListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.activityCreated();
        assertTrue(view.isEmptyViewToList);
    }

    @Test
    public void Under0PositionReturnsMinusOneWhenGetCurationId() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        CurationListPresenter presenter = new CurationListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        assertThat(presenter.getCurationIdAt(-1), is(-1));
    }

    @Test
    public void BiggerIndexThanViewSizeReturnsMinusOneWhenGetCurationId() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Curation> curations = new ArrayList<>();
        String testName = "testCuration";
        curations.add(new Curation(1, testName));
        when(adapter.getAllCurations()).thenReturn(curations);
        CurationListPresenter presenter = new CurationListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        assertThat(presenter.getCurationIdAt(curations.size()), is(-1));
    }

    @Test
    public void CurationOfIndexWhenGetCurationId() {
        DatabaseAdapter adapter = Mockito.mock(DatabaseAdapter.class);
        ArrayList<Curation> curations = new ArrayList<>();
        int testId = 1;
        curations.add(new Curation(testId, "testName"));
        when(adapter.getAllCurations()).thenReturn(curations);
        CurationListPresenter presenter = new CurationListPresenter(adapter);
        MockView view = new MockView();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        assertThat(presenter.getCurationIdAt(0), is(testId));
    }

    private class MockView implements CurationListView {
        private ArrayList<Curation> curations;
        private static final int DEFAULT_EDIT_ID = -1000;
        private int startedEditCurationId = DEFAULT_EDIT_ID;
        private boolean isNoRssViewSet = false;
        private boolean isEmptyViewToList = false;

        @Override
        public void startEditCurationActivity(int editCurationId) {
            startedEditCurationId = editCurationId;
        }

        @Override
        public void setNoRssTextToEmptyView() {
            isNoRssViewSet = true;
        }

        @Override
        public void setEmptyViewToList() {
            isEmptyViewToList = true;
        }

        @Override
        public void registerContextMenu() {

        }

        @Override
        public void initListBy(ArrayList<Curation> curations) {
            this.curations = curations;
        }

        @Override
        public void delete(Curation curation) {
            curations.remove(curation);
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Curation curationAt(int position) {
            return curations.get(position);
        }
    }
}
