package com.phicdy.mycuration.presentation.presenter;

import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.presentation.view.SelectTargetRssView;

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

    public void optionItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done_select_target_rss:
                view.finishSelect();
        }
    }
}
