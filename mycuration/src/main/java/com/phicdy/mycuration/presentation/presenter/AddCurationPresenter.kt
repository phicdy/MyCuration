package com.phicdy.mycuration.presentation.presenter


import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.util.TextUtil
import com.phicdy.mycuration.presentation.view.AddCurationView
import kotlinx.coroutines.coroutineScope


class AddCurationPresenter(
        private val view: AddCurationView,
        private val adapter: DatabaseAdapter,
        private val repository: CurationRepository
) : Presenter {

    companion object {
        const val NOT_EDIT_CURATION_ID = -1
    }

    private var editCurationid = NOT_EDIT_CURATION_ID
    private var addedWords = ArrayList<String>()

    override fun create() {
        editCurationid = view.editCurationId()
    }

    fun activityCreated() {
        view.initView()
        view.refreshList(addedWords)
    }

    override fun resume() {
        if (editCurationid != NOT_EDIT_CURATION_ID) {
            view.setCurationName(adapter.getCurationNameById(editCurationid))
            addedWords = adapter.getCurationWords(editCurationid)
            view.refreshList(addedWords)
        }
        view.refreshList(addedWords)
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
        if (addedWords.contains(word)) {
            view.showDupulicatedWordToast()
            return
        }
        addedWords.add(word)
        view.refreshList(addedWords)
        view.resetInputWord()
    }

    suspend fun onAddMenuClicked() = coroutineScope {
        val curationName = view.curationName()
        if (TextUtil.isEmpty(curationName)) {
            view.handleEmptyCurationNameError()
            return@coroutineScope
        }
        if (addedWords.size == 0) {
            view.handleEmptyWordError()
            return@coroutineScope
        }

        val isNew = editCurationid == AddCurationPresenter.NOT_EDIT_CURATION_ID
        if (isNew && adapter.isExistSameNameCuration(curationName)) {
            view.handleSameNameCurationError()
            return@coroutineScope
        }
        val result = if (isNew) {
            repository.store(curationName, addedWords)
        } else {
            repository.update(editCurationid, curationName, addedWords)
        }
        if (result) {
            adapter.adaptCurationToArticles(curationName, addedWords)
            if (isNew) {
                view.handleAddSuccess()
            } else {
                view.handleEditSuccess()
            }
        }
    }

    fun onDeleteButtonClicked(position: Int) {
        addedWords.removeAt(position)
        view.refreshList(addedWords)
    }
}
