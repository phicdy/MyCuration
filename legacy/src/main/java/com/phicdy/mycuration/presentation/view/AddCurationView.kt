package com.phicdy.mycuration.presentation.view

interface AddCurationView {
    fun curationName(): String
    fun resetInputWord()
    fun handleEmptyCurationNameError()
    fun handleEmptyWordError()
    fun handleSameNameCurationError()
    fun handleAddSuccess()
    fun handleEditSuccess()
    fun showSuccessToast()
    fun showErrorToast()
    fun showWordEmptyErrorToast()
    fun showToast(text: String)
    fun showDupulicatedWordToast()
    fun showProgressDialog()
    fun dismissProgressDialog()
    fun finish()
}
