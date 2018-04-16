package com.phicdy.mycuration.presentation.presenter


import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.util.TextUtil
import com.phicdy.mycuration.presentation.view.AddCurationView


class AddCurationPresenter(private val view: AddCurationView, private val adapter: DatabaseAdapter) : Presenter {

    companion object {
        const val NOT_EDIT_CURATION_ID = -1
        const val INSERT_ERROR_MESSAGE = "insertErrorMessage"
    }

    private var editCurationid = NOT_EDIT_CURATION_ID

    override fun create() {
        editCurationid = view.editCurationId()
        if (editCurationid != NOT_EDIT_CURATION_ID) {
            view.setTitleForEdit()
        }
    }

    override fun resume() {
        if (editCurationid != NOT_EDIT_CURATION_ID) {
            view.setCurationName(adapter.getCurationNameById(editCurationid))
            view.setWords(adapter.getCurationWords(editCurationid))
        }
        view.refreshList()
    }

    override fun pause() {}

    fun handleInsertResultMessage(result: Boolean, errorMessage: String) {
        if (result) {
            view.showSuccessToast()
            view.dismissProgressDialog()
            view.finish()
        } else {
            view.showToast(errorMessage)
            view.showErrorToast()
            view.dismissProgressDialog()
        }
    }

    fun onAddWordButtonClicked() {
        val word = view.inputWord()
        if (word == "") {
            view.showWordEmptyErrorToast()
            return
        }
        view.addWord(word)
        view.resetInputWord()
    }

    fun onAddMenuClicked() {
        val curationName = view.curationName()
        if (TextUtil.isEmpty(curationName)) {
            view.handleEmptyCurationNameError()
            return
        }
        val wordList = view.wordList()
        if (wordList.size == 0) {
            view.handleEmptyWordError()
            return
        }

        val isNew = editCurationid == AddCurationPresenter.NOT_EDIT_CURATION_ID
        if (isNew && adapter.isExistSameNameCuration(curationName)) {
            view.handleSameNameCurationError()
            return
        }
        val result = if (isNew) {
            adapter.saveNewCuration(curationName, wordList)
        } else {
            adapter.updateCuration(editCurationid, curationName, wordList)
        }
        if (result) {
            adapter.adaptCurationToArticles(curationName, wordList)
            if (isNew) {
                view.handleAddSuccess()
            } else {
                view.handleEditSuccess()
            }
        }
    }
}
