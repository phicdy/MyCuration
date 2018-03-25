package com.phicdy.mycuration.presentation.view;


public interface TopActivityView {
    void initViewPager();
    void setAlarmManager();
    void closeSearchView();
    void goToFeedSearch();
    void goToAddCuration();
    void goToAddFilter();
    void goToSetting();
    void changeTab(int position);
    int currentTabPosition();
}
