package com.phicdy.mycuration.view;

import com.phicdy.mycuration.rss.Curation;

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
