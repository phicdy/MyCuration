package com.phicdy.mycuration.presentation.presenter;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Curation;
import com.phicdy.mycuration.presentation.view.CurationListView;

import java.util.ArrayList;

public class CurationListPresenter implements Presenter {

    private final DatabaseAdapter dbAdapter;
    private CurationListView view;

    public CurationListPresenter(DatabaseAdapter dbAdapter) {
        this.dbAdapter = dbAdapter;
    }

    public void setView(@NonNull CurationListView view) {
        this.view = view;
    }

    @Override
    public void create() {
    }

    @Override
    public void resume() {
        view.registerContextMenu();
        ArrayList<Curation> allCurations = dbAdapter.getAllCurations();
        view.initListBy(allCurations);
    }

    @Override
    public void pause() {
    }

    public void onCurationEditClicked(int curationId) {
        if (curationId < 0) return;
        view.startEditCurationActivity(curationId);
    }

    public void onCurationDeleteClicked(@NonNull Curation curation) {
        dbAdapter.deleteCuration(curation.getId());
        view.delete(curation);
    }

    public void activityCreated() {
        if (dbAdapter.getNumOfFeeds() == 0) {
            view.setNoRssTextToEmptyView();
        }
        view.setEmptyViewToList();
    }

    public int getCurationIdAt(int position) {
        if (position < 0 || position > view.size()) {
            return -1;
        }

        Curation curation = view.curationAt(position);
        if (curation == null) return -1;
        return curation.getId();
    }
}
