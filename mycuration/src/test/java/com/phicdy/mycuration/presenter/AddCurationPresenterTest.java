package com.phicdy.mycuration.presenter;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.view.AddCurationView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class AddCurationPresenterTest {

    private CurationWordListPresenter presenter;
    private DatabaseAdapter adapter;

    private static final String TITLE_FOR_ADD = "titleForAdd";
    private static final String TITLE_FOR_EDIT = "titleForEdit";
    private static final int TEST_EDIT_CURATION_ID = 10000;
    private static final String TEST_EDIT_CURATION_NAME = "testCurationName";

    @Before
    public void setup() {
        adapter = Mockito.mock(DatabaseAdapter.class);
        presenter = new CurationWordListPresenter(adapter);
    }

    @Test
    public void DefaultTitleIsTitleForAdd() {
        // Go to onCreate() in add status
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
        presenter.create();
        assertEquals(view.title, TITLE_FOR_ADD);
    }

    @Test
    public void WhenEditTitleBecomesEdit() {
        // Go to onCreate() in edit status
        MockViewAdd view = new MockViewAdd();
        view.setEditCurationId(TEST_EDIT_CURATION_ID);
        presenter.setView(view);
        presenter.create();
        assertEquals(view.title, TITLE_FOR_EDIT);
    }

    @Test
    public void DefaultCurationNameIsEmpty() {
        // Go to onResume() in add status
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        assertEquals(view.curationName, "");
    }

    @Test
    public void WhenEditCurationNameBecomesStoredOne() {
        // Mock to return test curation
        MockViewAdd view = new MockViewAdd();
        view.editCurationId = TEST_EDIT_CURATION_ID;
        Mockito.when(adapter.getCurationNameById(TEST_EDIT_CURATION_ID)).thenReturn(TEST_EDIT_CURATION_NAME);

        // Go to onResume() in edit status
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        assertEquals(view.curationName, TEST_EDIT_CURATION_NAME);
    }

    @Test
    public void WhenInsertSucceedsToastShows() {
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handleInsertResultMessage(true, "");
        assertTrue(view.isSuccessToastShowed);
    }

    @Test
    public void WhenInsertSucceedsProgressDialogDismisses() {
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handleInsertResultMessage(true, "");
        assertFalse(view.isProgressDialogShowed);
    }

    @Test
    public void WhenInsertSucceedsViewFinishes() {
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handleInsertResultMessage(true, "");
        assertTrue(view.isFinished);
    }

    @Test
    public void WhenInsertFailsErrorToastShows() {
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handleInsertResultMessage(false, "Insert Error");
        assertEquals(view.errorToastMessage, MockViewAdd.ERROR_ADD);
    }

    @Test
    public void WhenInsertFailsProgressDialogDismisses() {
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handleInsertResultMessage(false, "Insert Error");
        assertFalse(view.isProgressDialogShowed);
    }

    @Test
    public void WhenInsertFailsViewStillShows() {
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.handleInsertResultMessage(false, "Insert Error");
        assertFalse(view.isFinished);
    }

    @Test
    public void WhenAddWordButtonClickedWithEmptyErrorToastShows() {
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.onAddWordButtonClicked();
        assertTrue(view.isEmptyWordErrorToastShowed);
    }

    @Test
    public void WhenAddWordButtonClickedWordIsAdded() {
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        view.word = "test";
        presenter.onAddWordButtonClicked();
        assertEquals(view.wordList().get(0), "test");
    }

    @Test
    public void WhenAddWordButtonClickedTwiceWordAreAdded() {
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        view.word = "test";
        presenter.onAddWordButtonClicked();
        view.word = "test2";
        presenter.onAddWordButtonClicked();
        assertEquals(view.wordList().size(), 2);
    }

    @Test
    public void WhenAddWordButtonClickedInputFieldBecomesEmpty() {
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        view.word = "test";
        presenter.onAddWordButtonClicked();
        assertEquals(view.word, "");
    }

    @Test
    public void WhenAddButtonClickedWithEmptyNameErrorToastShows() {
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        presenter.onAddMenuClicked();
        assertTrue(view.isEmptyNameErrorToastShowed);
    }

    @Test
    public void WhenAddButtonClickedWithCurationNameAndNoWordsErrorToastShows() {
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
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

        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
        presenter.create();
        presenter.resume();
        view.curationName = TEST_EDIT_CURATION_NAME;
        view.words.add("word");
        presenter.onAddMenuClicked();
        assertTrue(view.isSameNameErrorToastShowed);
    }

    @Test
    public void WhenAddButtonClickedAndSucceedsSuccessToastShows() {
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
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
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
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
        MockViewAdd view = new MockViewAdd();
        presenter.setView(view);
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
        MockViewAdd view = new MockViewAdd();
        view.editCurationId = TEST_EDIT_CURATION_ID;
        presenter.setView(view);
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
    public void WhenEditSucceedsProgressDialogDissmisses() {
        MockViewAdd view = new MockViewAdd();
        view.editCurationId = TEST_EDIT_CURATION_ID;
        presenter.setView(view);
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
        MockViewAdd view = new MockViewAdd();
        view.editCurationId = TEST_EDIT_CURATION_ID;
        presenter.setView(view);
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
        private int editCurationId = CurationWordListPresenter.NOT_EDIT_CURATION_ID;
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
        public ArrayList<String> wordList() {
            return words;
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
        public void refreshList() {
        }

        @Override
        public void addWord(String word) {
            words.add(word);
        }

        @Override
        public void setWords(ArrayList<String> words) {
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
    }
}
