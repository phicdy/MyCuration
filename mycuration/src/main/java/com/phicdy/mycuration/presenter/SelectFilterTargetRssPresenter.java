package com.phicdy.mycuration.presenter;

import android.support.annotation.NonNull;

import com.phicdy.mycuration.view.SelectTargetRssView;

public class SelectFilterTargetRssPresenter implements Presenter {

    private SelectTargetRssView view;

    public SelectFilterTargetRssPresenter() {
    }

    public void setView(@NonNull SelectTargetRssView view) {
        this.view = view;
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

}
