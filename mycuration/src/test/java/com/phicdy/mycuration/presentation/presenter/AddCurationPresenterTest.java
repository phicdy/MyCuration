package com.phicdy.mycuration.presentation.presenter;

import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.presentation.view.AddCurationView;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class AddCurationPresenterTest {

    private AddCurationPresenter presenter;
    private DatabaseAdapter adapter;
    private MockViewAdd view;

    private static final String TITLE_FOR_ADD = "titleForAdd";
    private static final String TITLE_FOR_EDIT = "titleForEdit";
    private static final int TEST_EDIT_CURATION_ID = 10000;
    private static final String TEST_EDIT_CURATION_NAME = "testCurationName";

    @Before
    public void setup() {
        view = new MockViewAdd();
        adapter = Mockito.mock(DatabaseAdapter.class);
        presenter = new AddCurationPresenter(view, adapter);
    }

    @Test
    public void DefaultTitleIsTitleForAdd() {
        // Go to onCreate() in add status
        presenter.create();
        assertEquals(view.title, TITLE_FOR_ADD);
    }

    @Test
    public void WhenEditTitleBecomesEdit() {
        // Go to onCreate() in edit status
        view.setEditCurationId(TEST_EDIT_CURATION_ID);
        presenter.create();
        assertEquals(view.title, TITLE_FOR_EDIT);
    }

    @Test
    public void DefaultCurationNameIsEmpty() {
        // Go to onResume() in add status
        presenter.create();
        presenter.resume();
        assertEquals(view.curationName, "");
    }

    @Test
    public void WhenEditCurationNameBecomesStoredOne() {
        // Mock to return test curation
        view.editCurationId = TEST_EDIT_CURATION_ID;
        Mockito.when(adapter.getCurationNameById(TEST_EDIT_CURATION_ID)).thenReturn(TEST_EDIT_CURATION_NAME);

        // Go to onResume() in edit status
        presenter.create();
        presenter.resume();
        assertEquals(view.curationName, TEST_EDIT_CURATION_NAME);
    }

    @Test
    public void WhenInsertSucceedsToastShows() {
        presenter.create();
        presenter.resume();
        presenter.handleInsertResultMessage(true, "");
        assertTrue(view.isSuccessToastShowed);
    }

    @Test
    public void WhenInsertSucceedsProgressDialogDismisses() {
        presenter.create();
        presenter.resume();
        presenter.handleInsertResultMessage(true, "");
        assertFalse(view.isProgressDialogShowed);
    }

    @Test
    public void WhenInsertSucceedsViewFinishes() {
        presenter.create();
        presenter.resume();
        presenter.handleInsertResultMessage(true, "");
        assertTrue(view.isFinished);
    }

    @Test
    public void WhenInsertFailsErrorToastShows() {
        presenter.create();
        presenter.resume();
        presenter.handleInsertResultMessage(false, "Insert Error");
        assertEquals(view.errorToastMessage, MockViewAdd.ERROR_ADD);
    }

    @Test
    public void WhenInsertFailsProgressDialogDismisses() {
        presenter.create();
        presenter.resume();
        presenter.handleInsertResultMessage(false, "Insert Error");
        assertFalse(view.isProgressDialogShowed);
    }

    @Test
    public void WhenInsertFailsViewStillShows() {
        presenter.create();
        presenter.resume();
        presenter.handleInsertResultMessage(false, "Insert Error");
        assertFalse(view.isFinished);
    }

    @Test
    public void WhenAddWordButtonClickedWithEmptyErrorToastShows() {
        presenter.create();
        presenter.resume();
        presenter.onAddWordButtonClicked();
        assertTrue(view.isEmptyWordErrorToastShowed);
    }

    @Test
    public void WhenAddWordButtonClickedWordIsAdded() {
        presenter.create();
        presenter.resume();
        view.word = "test";
        presenter.onAddWordButtonClicked();
        assertEquals(view.words.get(0), "test");
    }

    @Test
    public void WhenAddWordButtonClickedTwiceWordAreAdded() {
        presenter.create();
        presenter.resume();
        view.word = "test";
        presenter.onAddWordButtonClicked();
        view.word = "test2";
        presenter.onAddWordButtonClicked();
        assertEquals(view.words.size(), 2);
    }

    @Test
    public void WhenAddWordButtonClickedInputFieldBecomesEmpty() {
        presenter.create();
        presenter.resume();
        view.word = "test";
        presenter.onAddWordButtonClicked();
        assertEquals(view.word, "");
    }

    @Test
    public void WhenAddButtonClickedWithEmptyNameErrorToastShows() {
        presenter.create();
        presenter.resume();
        presenter.onAddMenuClicked();
        assertTrue(view.isEmptyNameErrorToastShowed);
    }

    @Test
    public void WhenAddButtonClickedWithCurationNameAndNoWordsErrorToastShows() {
        presenter.create();
        presenter.resume();
        view.curationName = "test";
        presenter.onAddMenuClicked();
        assertTrue(view.isNoWordsAddedErrorToastShowed);
    }

    @Test
    public void WhenAddButtonClickedWithExistingCurationNameAndWordsErrorToastShows() {
        // Mock test curation exists
        Mockito.when(adapter.isExistSameNameCuration(TEST_EDIT_CURATION_NAME)).thenReturn(true);

        presenter.create();
        presenter.resume();
        view.curationName = TEST_EDIT_CURATION_NAME;
        view.words.add("word");
        presenter.onAddMenuClicked();
        assertTrue(view.isSameNameErrorToastShowed);
    }

    @Test
    public void WhenAddButtonClickedAndSucceedsSuccessToastShows() {
        presenter.create();
        presenter.resume();
        view.curationName = TEST_EDIT_CURATION_NAME;
        view.words.add("word");
        // Mock save new curation
        Mockito.when(adapter.saveNewCuration(TEST_EDIT_CURATION_NAME, view.words)).thenReturn(true);
        presenter.onAddMenuClicked();
        assertTrue(view.isSuccessToastShowed);
    }

    @Test
    public void WhenAddButtonClickedAndSucceedsProgressDialogDissmisses() {
        presenter.create();
        presenter.resume();
        view.curationName = TEST_EDIT_CURATION_NAME;
        view.words.add("word");
        // Mock save new curation
        Mockito.when(adapter.saveNewCuration(TEST_EDIT_CURATION_NAME, view.words)).thenReturn(true);
        presenter.onAddMenuClicked();
        assertFalse(view.isProgressDialogShowed);
    }

    @Test
    public void WhenAddButtonClickedAndSucceedsViewFinishes() {
        presenter.create();
        presenter.resume();
        view.curationName = TEST_EDIT_CURATION_NAME;
        view.words.add("word");
        // Mock save new curation
        Mockito.when(adapter.saveNewCuration(TEST_EDIT_CURATION_NAME, view.words)).thenReturn(true);
        presenter.onAddMenuClicked();
        assertTrue(view.isFinished);
    }

    @Test
    public void WhenEditSucceedsSuccessToastShows() {
        view.editCurationId = TEST_EDIT_CURATION_ID;
        Mockito.when(adapter.getCurationNameById(TEST_EDIT_CURATION_ID)).thenReturn("test");
        presenter.create();
        presenter.resume();
        view.curationName = TEST_EDIT_CURATION_NAME;
        view.words.add("word");
        // Mock update stored curation
        Mockito.when(adapter.updateCuration(TEST_EDIT_CURATION_ID, TEST_EDIT_CURATION_NAME, view.words))
                .thenReturn(true);
        presenter.onAddMenuClicked();
        assertTrue(view.isSuccessToastShowed);
    }

    @Test
    public void testOnPause() {
        // For coverage
        presenter.pause();
    }

    @Test
    public void WhenEditSucceedsProgressDialogDissmisses() {
        view.editCurationId = TEST_EDIT_CURATION_ID;
        Mockito.when(adapter.getCurationNameById(TEST_EDIT_CURATION_ID)).thenReturn("test");
        presenter = new AddCurationPresenter(view, adapter);
        presenter.create();
        presenter.resume();
        view.curationName = TEST_EDIT_CURATION_NAME;
        view.words.add("word");
        // Mock update stored curation
        Mockito.when(adapter.updateCuration(TEST_EDIT_CURATION_ID, TEST_EDIT_CURATION_NAME, view.words))
                .thenReturn(true);
        presenter.onAddMenuClicked();
        assertFalse(view.isProgressDialogShowed);
    }

    @Test
    public void WhenEditSucceedsViewFinishes() {
        view.editCurationId = TEST_EDIT_CURATION_ID;
        Mockito.when(adapter.getCurationNameById(TEST_EDIT_CURATION_ID)).thenReturn("test");
        presenter = new AddCurationPresenter(view, adapter);
        presenter.create();
        presenter.resume();
        view.curationName = TEST_EDIT_CURATION_NAME;
        view.words.add("word");
        // Mock update stored curation
        Mockito.when(adapter.updateCuration(TEST_EDIT_CURATION_ID, TEST_EDIT_CURATION_NAME, view.words))
                .thenReturn(true);
        presenter.onAddMenuClicked();
        assertTrue(view.isFinished);
    }

    private class MockViewAdd implements AddCurationView {

        private String title = TITLE_FOR_ADD;
        private String curationName = "";
        private String word = "";
        private String errorToastMessage = "";
        private ArrayList<String> words = new ArrayList<>();
        private int editCurationId = AddCurationPresenter.NOT_EDIT_CURATION_ID;
        private boolean isSuccessToastShowed = false;
        private boolean isEmptyWordErrorToastShowed = false;
        private boolean isNoWordsAddedErrorToastShowed = false;
        private boolean isEmptyNameErrorToastShowed = false;
        private boolean isSameNameErrorToastShowed = false;
        private boolean isProgressDialogShowed = false;
        private boolean isFinished = false;

        static final String ERROR_ADD = "ERROR_ADD";

        @Override
        public int editCurationId() {
            return editCurationId;
        }

        void setEditCurationId(int id) {
            this.editCurationId = id;
        }

        @Override
        public String inputWord() {
            return word;
        }

        @Override
        public String curationName() {
            return curationName;
        }

        @Override
        public void setCurationName(String name) {
            this.curationName = name;
        }

        @Override
        public void resetInputWord() {
            word = "";
        }

        @Override
        public void setTitleForEdit() {
            title = TITLE_FOR_EDIT;
        }

        @Override
        public void handleEmptyCurationNameError() {
            isEmptyNameErrorToastShowed = true;
        }

        @Override
        public void handleEmptyWordError() {
            isNoWordsAddedErrorToastShowed = true;
        }

        @Override
        public void handleSameNameCurationError() {
            isSameNameErrorToastShowed = true;
        }

        @Override
        public void handleAddSuccess() {
            showSuccessToast();
            dismissProgressDialog();
            finish();
        }

        @Override
        public void handleEditSuccess() {
            showSuccessToast();
            dismissProgressDialog();
            finish();
        }

        @Override
        public void showSuccessToast() {
            isSuccessToastShowed = true;
        }

        @Override
        public void showErrorToast() {
            errorToastMessage = ERROR_ADD;
        }

        @Override
        public void showWordEmptyErrorToast() {
            isEmptyWordErrorToastShowed = true;
        }

        @Override
        public void showToast(String text) {
        }

        @Override
        public void showProgressDialog() {
            isProgressDialogShowed = true;
        }

        @Override
        public void dismissProgressDialog() {
            isProgressDialogShowed = false;
        }

        @Override
        public void finish() {
            isFinished = true;
        }

        @Override
        public void initView() {
        }

        @Override
        public void refreshList(@NotNull ArrayList<String> addedWords) {
            words = addedWords;
        }

        @Override
        public void showDupulicatedWordToast() {
        }
    }
}
