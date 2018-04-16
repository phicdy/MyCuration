package com.phicdy.mycuration.presentation.view

import java.util.ArrayList

interface AddCurationView {
    fun editCurationId(): Int
    fun inputWord(): String
    fun curationName(): String
    fun wordList(): ArrayList<String>
    fun setCurationName(name: String)
    fun resetInputWord()
    fun refreshList()
    fun addWord(word: String)
    fun setWords(words: ArrayList<String>)
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
    fun showProgressDialog()
    fun dismissProgressDialog()
    fun finish()
}
