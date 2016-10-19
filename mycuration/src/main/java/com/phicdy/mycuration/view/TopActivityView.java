package com.phicdy.mycuration.view;


import android.app.Activity;

public interface TopActivityView {
    Activity getActivity();
    void closeSearchView();
    String screenName();
    void goToFeedSearch();
    void goToAddCuration();
    void goToAddFilter();
    void goToSetting();
    void goToLicense();
    int currentTabPosition();
}
