package com.phicdy.mycuration.presentation.presenter;


import android.support.annotation.NonNull;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.util.TextUtil;
import com.phicdy.mycuration.presentation.view.AddCurationView;

import java.util.ArrayList;

public class AddCurationPresenter implements Presenter {

    private AddCurationView view;
    private final DatabaseAdapter adapter;

    private int editCurationid = NOT_EDIT_CURATION_ID;
    public static final int NOT_EDIT_CURATION_ID = -1;
    public static final String INSERT_ERROR_MESSAGE = "insertErrorMessage";

    public AddCurationPresenter(@NonNull DatabaseAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void create() {
        editCurationid = view.editCurationId();
        if (editCurationid != NOT_EDIT_CURATION_ID) {
            view.setTitleForEdit();
        }
    }

    @Override
    public void resume() {
        if (editCurationid != NOT_EDIT_CURATION_ID) {
            view.setCurationName(adapter.getCurationNameById(editCurationid));
            view.setWords(adapter.getCurationWords(editCurationid));
        }
        view.refreshList();
    }

    @Override
    public void pause() {
    }

    public void setView(AddCurationView view) {
        this.view = view;
    }

    public void handleInsertResultMessage(boolean result, String errorMessage) {
        if (result) {
            view.showSuccessToast();
            view.dismissProgressDialog();
            view.finish();
        }else {
            view.showToast(errorMessage);
            view.showErrorToast();
            view.dismissProgressDialog();
        }
    }

    public void onAddWordButtonClicked() {
        String word = view.inputWord();
        if (word.equals("")) {
            view.showWordEmptyErrorToast();
            return;
        }
        view.addWord(word);
        view.resetInputWord();
    }

    public void onAddMenuClicked() {
        String curationName = view.curationName();
        if (TextUtil.INSTANCE.isEmpty(curationName)) {
            view.handleEmptyCurationNameError();
            return;
        }
        ArrayList<String> wordList = view.wordList();
        if (wordList == null || wordList.size() == 0) {
            view.handleEmptyWordError();
            return;
        }

        boolean isNew = (editCurationid == AddCurationPresenter.NOT_EDIT_CURATION_ID);
        if (isNew && adapter.isExistSameNameCuration(curationName)) {
            view.handleSameNameCurationError();
            return;
        }
        boolean result;
        if (isNew) {
            result = adapter.saveNewCuration(curationName, wordList);
        }else {
            result = adapter.updateCuration(editCurationid, curationName, wordList);
        }
        if (result) {
            adapter.adaptCurationToArticles(curationName, wordList);
            if (isNew) {
                view.handleAddSuccess();
            } else {
                view.handleEditSuccess();
            }
        }
    }
}
