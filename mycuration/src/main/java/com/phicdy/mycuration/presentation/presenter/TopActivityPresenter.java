package com.phicdy.mycuration.presentation.presenter;

import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.presentation.view.TopActivityView;

public class TopActivityPresenter implements Presenter {

    private TopActivityView view;
    private DatabaseAdapter dbAdapter;
    private int launchTab = POSITION_CURATION_FRAGMENT;

    private static final int POSITION_CURATION_FRAGMENT = 0;
    private static final int POSITION_FEED_FRAGMENT = 1;
    private static final int POSITION_FILTER_FRAGMENT = 2;

    public TopActivityPresenter(int launchTab) {
        this.launchTab = launchTab;
    }

    public void setView(@NonNull TopActivityView view) {
        this.view = view;
    }

    @Override
    public void create() {
        view.initViewPager();
        view.initFab();
        view.setAlarmManager();
        view.changeTab(launchTab);
    }

    @Override
    public void resume() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                dbAdapter.saveAllStatusToReadFromToRead();
            }
        }).start();

        view.closeSearchView();
    }

    @Override
    public void pause() {

    }

    public void setDataAdapter(DatabaseAdapter adapter) {
        this.dbAdapter = adapter;
    }

    public void fabClicked() {
        switch (view.currentTabPosition()) {
            case POSITION_CURATION_FRAGMENT:
                if (dbAdapter.getNumOfFeeds() == 0) {
                    view.goToFeedSearch();
                    return;
                }
                view.goToAddCuration();
                break;
            case POSITION_FEED_FRAGMENT:
                view.goToFeedSearch();
                break;
            case POSITION_FILTER_FRAGMENT:
                if (dbAdapter.getNumOfFeeds() == 0) {
                    view.goToFeedSearch();
                    break;
                }
                view.goToAddFilter();
                break;
            default:
        }
    }

    private void settingMenuClicked() {
        view.goToSetting();
    }

    public void optionItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting_top_activity:
                settingMenuClicked();
                break;
            default:
        }
    }
}
