package com.phicdy.mycuration.presenter;

import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.view.TopActivityView;

public class TopActivityPresenter implements Presenter {

    private TopActivityView view;
    private GATrackerHelper gaTrackerHelper;
    private DatabaseAdapter dbAdapter;

    private static final int POSITION_CURATION_FRAGMENT = 0;
    private static final int POSITION_FEED_FRAGMENT = 1;
    private static final int POSITION_FILTER_FRAGMENT = 2;

    public TopActivityPresenter() {

    }

    public void setView(@NonNull TopActivityView view) {
        this.view = view;
    }

    @Override
    public void create() {
        gaTrackerHelper = GATrackerHelper.getInstance(view.getActivity());
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
        gaTrackerHelper.sendScreen(view.screenName());
    }

    @Override
    public void pause() {

    }

    public void setDataAdapter(DatabaseAdapter adapter) {
        this.dbAdapter = adapter;
    }

    private void addMenuClicked() {
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

    private void licenseMenuClicked() {
        view.goToLicense();
    }

    public void optionItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                addMenuClicked();
                break;
            case R.id.setting:
                settingMenuClicked();
                break;
            case R.id.license:
                licenseMenuClicked();
                break;
            default:
        }
    }
}
