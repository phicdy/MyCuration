package com.phicdy.mycuration.presentation.presenter;

import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.filter.Filter;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.presentation.view.RegisterFilterView;

import java.util.ArrayList;

public class RegisterFilterPresenter implements Presenter {

    private RegisterFilterView view;
    private final DatabaseAdapter dbAdapter;
    private ArrayList<Feed> selectedFeedList = new ArrayList<>();

    private int editFilterId = NEW_FILTER_ID;
    private boolean isEdit = false;
    private static final int NEW_FILTER_ID = -1;

    public RegisterFilterPresenter(DatabaseAdapter adapter, int editFilterId) {
        this.dbAdapter = adapter;
        this.editFilterId = editFilterId;
        isEdit = editFilterId != NEW_FILTER_ID;
    }

    public void setView(@NonNull RegisterFilterView view) {
        this.view = view;
        if (isEdit) {
            Filter editFilter = dbAdapter.getFilterById(editFilterId);
            if (editFilter != null) {
                view.setFilterTitle(editFilter.getTitle());
                view.setFilterUrl(editFilter.getUrl());
                view.setFilterKeyword(editFilter.getKeyword());
                setSelectedFeedList(editFilter.feeds());
            }
        }
    }

    public void setSelectedFeedList(ArrayList<Feed> list) {
        this.selectedFeedList = list;
        setTargetRssTitle(selectedFeedList);
    }

    public ArrayList<Feed> selectedFeedList() {
        return selectedFeedList;
    }

    private void setTargetRssTitle(ArrayList<Feed> feeds) {
        if (feeds == null || feeds.size() == 0) {
            view.resetFilterTargetRss();
        } else if (feeds.size() == 1) {
            view.setFilterTargetRss(feeds.get(0).getTitle());
        } else {
            view.setMultipleFilterTargetRss();
        }
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
            boolean result;
            if (isEdit) {
                result = dbAdapter.updateFilter(editFilterId, titleText, keywordText, filterUrlText, selectedFeedList);
                view.trackEdit();
            } else {
                // Add new filter
                result = dbAdapter.saveNewFilter(titleText, selectedFeedList, keywordText, filterUrlText);
                view.trackRegister();
            }
            if (result) {
                view.showSaveSuccessToast();
            }else {
                view.showSaveErrorToast();
            }
            view.finish();
        }
    }

    public void optionItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_filter:
                addMenuClicked();
                break;
            default:
        }
    }
}
