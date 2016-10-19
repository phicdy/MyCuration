package com.phicdy.mycuration.presenter;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.view.TopActivityView;

public class TopActivityPresenter implements Presenter {

    private TopActivityView view;
    private GATrackerHelper gaTrackerHelper;
    private DatabaseAdapter dbAdapter;

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
}
