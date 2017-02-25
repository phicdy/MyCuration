package com.phicdy.mycuration.view;

import java.util.ArrayList;

public interface AddCurationView {
    int editCurationId();
    String inputWord();
    String curationName();
    ArrayList<String> wordList();
    void setCurationName(String name);
    void resetInputWord();
    void refreshList();
    void addWord(String word);
    void setWords(ArrayList<String> words);
    void setTitleForEdit();
    void handleEmptyCurationNameError();
    void handleEmptyWordError();
    void handleSameNameCurationError();
    void handleAddSuccess();
    void handleEditSuccess();
    void showSuccessToast();
    void showErrorToast();
    void showWordEmptyErrorToast();
    void showToast(String text);
    void showProgressDialog();
    void dismissProgressDialog();
    void finish();
}
