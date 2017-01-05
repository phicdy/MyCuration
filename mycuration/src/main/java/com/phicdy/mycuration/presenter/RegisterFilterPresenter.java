package com.phicdy.mycuration.presenter;

import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.view.RegisterFilterView;

import java.util.ArrayList;

public class RegisterFilterPresenter implements Presenter {

    private RegisterFilterView view;
    private DatabaseAdapter dbAdapter;
    private ArrayList<Feed> selectedFeedList = new ArrayList<>();

    private int editFilterId = NEW_FILTER_ID;
    private static final int NEW_FILTER_ID = -1;

    public RegisterFilterPresenter(DatabaseAdapter adapter, int editFilterId) {
        this.dbAdapter = adapter;
        this.editFilterId = editFilterId;
    }

    public void setView(@NonNull RegisterFilterView view) {
        this.view = view;
    }

    public void setSelectedFeedList(ArrayList<Feed> list) {
        this.selectedFeedList = list;
    }

    @Override
    public void create() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {

    }

    private void addMenuClicked() {
        String keywordText = view.filterKeyword();
        String filterUrlText = view.filterUrl();
        String titleText = view.filterTitle();

        //Check title and etKeyword or filter URL has the text
        if (titleText.equals("")) {
            view.handleEmptyTitle();
        } else if ((keywordText.equals("")) && (filterUrlText.equals(""))) {
            view.handleEmptyCondition();
        } else if (keywordText.equals("%") || filterUrlText.equals("%")) {
            view.handlePercentOnly();
        } else {
            boolean finalResult = false;
            for (Feed feed : selectedFeedList) {
                boolean result = false;
                if (editFilterId == NEW_FILTER_ID) {
                    // Add new filter
                    result = dbAdapter.saveNewFilter(titleText, feed.getId(), keywordText, filterUrlText);
                    view.trackRegister();
                } else {
                    // Edit
                    result = dbAdapter.updateFilter(editFilterId, titleText, keywordText, filterUrlText, feed.getId());
                    view.trackEdit();
                }
                finalResult = finalResult && result;
            }
            if (finalResult) {
                view.showSaveSuccessToast();
            }else {
                view.showSaveErrorToast();
            }
            view.finish();
        }
    }

    public void optionItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                addMenuClicked();
                break;
            default:
        }
    }
}
