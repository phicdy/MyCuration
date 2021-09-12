package com.phicdy.mycuration.presentation.presenter


import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.presentation.view.AddCurationView
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject


class AddCurationPresenter @Inject constructor(
        private val view: AddCurationView,
        private val repository: CurationRepository
) {

    companion object {
        const val NOT_EDIT_CURATION_ID = -1
    }

    private var editCurationid = NOT_EDIT_CURATION_ID
    private var addedWords = ArrayList<String>()

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

    suspend fun onAddMenuClicked() = coroutineScope {
        val curationName = view.curationName()
        if (curationName.isEmpty()) {
            view.handleEmptyCurationNameError()
            return@coroutineScope
        }
        if (addedWords.size == 0) {
            view.handleEmptyWordError()
            return@coroutineScope
        }

        val isNew = editCurationid == AddCurationPresenter.NOT_EDIT_CURATION_ID
        if (isNew && repository.isExist(curationName)) {
            view.handleSameNameCurationError()
            return@coroutineScope
        }
        val id = if (isNew) {
            repository.store(curationName, addedWords).toInt()
        } else {
            repository.update(editCurationid, curationName, addedWords)
            editCurationid
        }
        if (id > 0) {
            repository.adaptToArticles(id, addedWords)
            if (isNew) {
                view.handleAddSuccess()
            } else {
                view.handleEditSuccess()
            }
        }
    }
}
