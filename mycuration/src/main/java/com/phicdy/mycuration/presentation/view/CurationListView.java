package com.phicdy.mycuration.presentation.view;

import com.phicdy.mycuration.data.rss.Curation;

import java.util.ArrayList;

public interface CurationListView {
    void startEditCurationActivity(int editCurationId);
    void setNoRssTextToEmptyView();
    void setEmptyViewToList();
    void registerContextMenu();
    void initListBy(ArrayList<Curation> curations);
    void delete(Curation curation);
    int size();
    Curation curationAt(int position);
}
