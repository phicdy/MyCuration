package com.phicdy.mycuration.presentation.presenter

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.presentation.view.AddCurationView
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.ArrayList


class AddCurationPresenterTest {

    private lateinit var presenter: AddCurationPresenter
    private lateinit var adapter: DatabaseAdapter
    private lateinit var view: MockViewAdd
    private lateinit var repository: CurationRepository

    @Before
    fun setup() {
        view = MockViewAdd()
        adapter = mock(DatabaseAdapter::class.java)
        repository = mock(CurationRepository::class.java)
        presenter = AddCurationPresenter(view, adapter, repository)
    }

    @Test
    fun `when onCreate then default title is title for add`() {
        // Go to onCreate() in add status
        presenter.create()
        assertEquals(view.title, TITLE_FOR_ADD)
    }

    @Test
    fun `when onResume default curation name is empty`() = runBlocking {
        // Go to onResume() in add status
        presenter.create()
        presenter.resume()
        assertEquals(view.curationName(), "")
    }

    @Test
    fun `when edit curation then the name becomes stored one`() = runBlocking {
        // Mock to return test curation
        view.editCurationId = TEST_EDIT_CURATION_ID
        `when`(repository.getCurationNameById(TEST_EDIT_CURATION_ID)).thenReturn(TEST_EDIT_CURATION_NAME)

        // Go to onResume() in edit status
        presenter.create()
        presenter.resume()
        assertEquals(view.curationName(), TEST_EDIT_CURATION_NAME)
    }

    @Test
    fun `when insert succeeds then toast shows`() = runBlocking {
        presenter.create()
        presenter.resume()
        presenter.handleInsertResultMessage(true, "")
        assertTrue(view.isSuccessToastShowed)
    }

    @Test
    fun `when insert succeeds then progress dialog dismisses`() = runBlocking {
        presenter.create()
        presenter.resume()
        presenter.handleInsertResultMessage(true, "")
        assertFalse(view.isProgressDialogShowed)
    }

    @Test
    fun `when insert succeeds then view finishes`() = runBlocking {
        presenter.create()
        presenter.resume()
        presenter.handleInsertResultMessage(true, "")
        assertTrue(view.isFinished)
    }

    @Test
    fun `when insert fails then error toast shows`() = runBlocking {
        presenter.create()
        presenter.resume()
        presenter.handleInsertResultMessage(false, "Insert Error")
        assertEquals(view.errorToastMessage, MockViewAdd.ERROR_ADD)
    }

    @Test
    fun `when insert fails then progress dialog dismissses`() = runBlocking {
        presenter.create()
        presenter.resume()
        presenter.handleInsertResultMessage(false, "Insert Error")
        assertFalse(view.isProgressDialogShowed)
    }

    @Test
    fun `when insert fails then view still shows`() = runBlocking {
        presenter.create()
        presenter.resume()
        presenter.handleInsertResultMessage(false, "Insert Error")
        assertFalse(view.isFinished)
    }

    @Test
    fun `when add word button is clicked in empty then error toast shows`() = runBlocking {
        presenter.create()
        presenter.resume()
        presenter.onAddWordButtonClicked()
        assertTrue(view.isEmptyWordErrorToastShowed)
    }

    @Test
    fun `when add word button is clicked then the word is added`() = runBlocking {
        presenter.create()
        presenter.resume()
        view.word = "test"
        presenter.onAddWordButtonClicked()
        assertEquals(view.words[0], "test")
    }

    @Test
    fun `when add word button is clicked then twice words are added`() = runBlocking {
        presenter.create()
        presenter.resume()
        view.word = "test"
        presenter.onAddWordButtonClicked()
        view.word = "test2"
        presenter.onAddWordButtonClicked()
        assertEquals(view.words.size, 2)
    }

    @Test
    fun `when add word button is clicked then input field becomes empty`() = runBlocking {
        presenter.create()
        presenter.resume()
        view.word = "test"
        presenter.onAddWordButtonClicked()
        assertEquals(view.word, "")
    }

    @Test
    fun `when add button is clicked in empty then name error toast shows`() = runBlocking {
        presenter.create()
        presenter.resume()
        presenter.onAddMenuClicked()
        assertTrue(view.isEmptyNameErrorToastShowed)
    }

    @Test
    fun `when add button is clicked with curation name and no words then error toast shows`() = runBlocking {
        presenter.create()
        presenter.resume()
        view.setCurationName("test")
        presenter.onAddMenuClicked()
        assertTrue(view.isNoWordsAddedErrorToastShowed)
    }

    @Test
    fun `when add button is clicked then with existing curation name and words then error toast shows`() = runBlocking {
        // Mock test curation exists
        `when`(repository.isExist(TEST_EDIT_CURATION_NAME)).thenReturn(true)

        presenter.create()
        presenter.resume()
        view.setCurationName(TEST_EDIT_CURATION_NAME)
        view.words.add("word")
        presenter.onAddMenuClicked()
        assertTrue(view.isSameNameErrorToastShowed)
    }

    @Test
    fun `when add button is clicked then and succeeds then success toast shows`() = runBlocking {
        presenter.create()
        presenter.resume()
        view.setCurationName(TEST_EDIT_CURATION_NAME)
        view.words.add("word")
        // Mock save new curation
        `when`(repository.store(TEST_EDIT_CURATION_NAME, view.words)).thenReturn(1)
        `when`(repository.isExist(TEST_EDIT_CURATION_NAME)).thenReturn(false)
        presenter.onAddMenuClicked()
        assertTrue(view.isSuccessToastShowed)
    }

    @Test
    fun `when add button is clicked then and succeeds then progress dialog dissmisses`() = runBlocking {
        presenter.create()
        presenter.resume()
        view.setCurationName(TEST_EDIT_CURATION_NAME)
        view.words.add("word")
        // Mock save new curation
        `when`(repository.store(TEST_EDIT_CURATION_NAME, view.words)).thenReturn(1)
        `when`(repository.isExist(TEST_EDIT_CURATION_NAME)).thenReturn(false)
        presenter.onAddMenuClicked()
        assertFalse(view.isProgressDialogShowed)
    }

    @Test
    fun `when add button is clicked then and succeeds then view finishes`() = runBlocking {
        presenter.create()
        presenter.resume()
        view.setCurationName(TEST_EDIT_CURATION_NAME)
        view.words.add("word")
        // Mock save new curation
        `when`(repository.store(TEST_EDIT_CURATION_NAME, view.words)).thenReturn(1)
        `when`(repository.isExist(TEST_EDIT_CURATION_NAME)).thenReturn(false)
        presenter.onAddMenuClicked()
        assertTrue(view.isFinished)
    }

    @Test
    fun `when edit succeeds then success toast shows`() = runBlocking {
        view.editCurationId = TEST_EDIT_CURATION_ID
        `when`(repository.getCurationNameById(TEST_EDIT_CURATION_ID)).thenReturn("test")
        presenter.create()
        presenter.resume()
        view.setCurationName(TEST_EDIT_CURATION_NAME)
        view.words.add("word")
        // Mock update stored curation
        `when`(repository.update(TEST_EDIT_CURATION_ID, TEST_EDIT_CURATION_NAME, view.words))
                .thenReturn(true)
        presenter.onAddMenuClicked()
        assertTrue(view.isSuccessToastShowed)
    }

    @Test
    fun `when edit succeeds then progress dialog dissmisses`() = runBlocking {
        view.editCurationId = TEST_EDIT_CURATION_ID
        `when`(repository.getCurationNameById(TEST_EDIT_CURATION_ID)).thenReturn("test")
        presenter = AddCurationPresenter(view, adapter, repository)
        presenter.create()
        presenter.resume()
        view.setCurationName(TEST_EDIT_CURATION_NAME)
        view.words.add("word")
        // Mock update stored curation
        `when`(repository.update(TEST_EDIT_CURATION_ID, TEST_EDIT_CURATION_NAME, view.words))
                .thenReturn(true)
        presenter.onAddMenuClicked()
        assertFalse(view.isProgressDialogShowed)
    }

    @Test
    fun `when edit succeeds then view finishes`() = runBlocking {
        view.editCurationId = TEST_EDIT_CURATION_ID
        `when`(repository.getCurationNameById(TEST_EDIT_CURATION_ID)).thenReturn("test")
        presenter = AddCurationPresenter(view, adapter, repository)
        presenter.create()
        presenter.resume()
        view.setCurationName(TEST_EDIT_CURATION_NAME)
        view.words.add("word")
        // Mock update stored curation
        `when`(repository.update(TEST_EDIT_CURATION_ID, TEST_EDIT_CURATION_NAME, view.words)).thenReturn(true)
        presenter.onAddMenuClicked()
        assertTrue(view.isFinished)
    }

    private class MockViewAdd : AddCurationView {

        val title = TITLE_FOR_ADD
        var _curationName = ""
        var word = ""
        var errorToastMessage = ""
        var words = ArrayList<String>()
        var editCurationId = AddCurationPresenter.NOT_EDIT_CURATION_ID
        var isSuccessToastShowed = false
        var isEmptyWordErrorToastShowed = false
        var isNoWordsAddedErrorToastShowed = false
        var isEmptyNameErrorToastShowed = false
        var isSameNameErrorToastShowed = false
        var isProgressDialogShowed = false
        var isFinished = false

        override fun editCurationId(): Int {
            return editCurationId
        }

        internal fun setEditCurationId(id: Int) {
            this.editCurationId = id
        }

        override fun inputWord(): String {
            return word
        }

        override fun curationName(): String {
            return _curationName
        }

        override fun setCurationName(name: String) {
            _curationName = name
        }

        override fun resetInputWord() {
            word = ""
        }

        override fun handleEmptyCurationNameError() {
            isEmptyNameErrorToastShowed = true
        }

        override fun handleEmptyWordError() {
            isNoWordsAddedErrorToastShowed = true
        }

        override fun handleSameNameCurationError() {
            isSameNameErrorToastShowed = true
        }

        override fun handleAddSuccess() {
            showSuccessToast()
            dismissProgressDialog()
            finish()
        }

        override fun handleEditSuccess() {
            showSuccessToast()
            dismissProgressDialog()
            finish()
        }

        override fun showSuccessToast() {
            isSuccessToastShowed = true
        }

        override fun showErrorToast() {
            errorToastMessage = ERROR_ADD
        }

        override fun showWordEmptyErrorToast() {
            isEmptyWordErrorToastShowed = true
        }

        override fun showToast(text: String) {}

        override fun showProgressDialog() {
            isProgressDialogShowed = true
        }

        override fun dismissProgressDialog() {
            isProgressDialogShowed = false
        }

        override fun finish() {
            isFinished = true
        }

        override fun initView() {}

        override fun refreshList(addedWords: ArrayList<String>) {
            words = addedWords
        }

        override fun showDupulicatedWordToast() {}

        companion object {
            const val ERROR_ADD = "ERROR_ADD"
        }
    }

    companion object {

        private const val TITLE_FOR_ADD = "titleForAdd"
        private const val TEST_EDIT_CURATION_ID = 10000
        private const val TEST_EDIT_CURATION_NAME = "testCurationName"
    }
}
