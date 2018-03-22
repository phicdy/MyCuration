package com.phicdy.mycuration.presentation.view;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.filter.Filter;

import java.util.ArrayList;

public interface FilterListView {
    void remove(int position);
    void notifyListChanged();
    void startEditActivity(int filterId);
    void initList(@NonNull ArrayList<Filter> filters);
}
