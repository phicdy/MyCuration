package com.phicdy.mycuration.presentation.view

interface AddCurationView {
    fun initView()
    fun editCurationId(): Int
    fun inputWord(): String
    fun curationName(): String
    fun setCurationName(name: String)
    fun resetInputWord()
    fun refreshList(addedWords: ArrayList<String>)
    fun setTitleForEdit()
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
