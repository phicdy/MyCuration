package com.phicdy.mycuration.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.presenter.SelectFilterTargetRssPresenter;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.view.SelectTargetRssView;

import java.util.ArrayList;

public class SelectFilterTargetRssActivity extends AppCompatActivity implements SelectTargetRssView {

    private SelectFilterTargetRssPresenter presenter;
    public static final String TARGET_RSS = "targetRss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_filter_target_rss);
        setTitle(getString(R.string.title_select_filter_rss));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        presenter = new SelectFilterTargetRssPresenter();
        presenter.setView(this);
        presenter.create();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        SelectFilterTargetRssFragment rssFragment = (SelectFilterTargetRssFragment) fragment;
        ArrayList<Feed> selectedList = getIntent().getParcelableArrayListExtra(TARGET_RSS);
        rssFragment.updateSelected(selectedList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select_filter_rss, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        presenter.optionItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finishSelect() {
        Intent data = new Intent();
        Bundle bundle = new Bundle();
        FragmentManager manager = getSupportFragmentManager();
        SelectFilterTargetRssFragment fragment =
                (SelectFilterTargetRssFragment) manager.findFragmentById(R.id.f_select_target);
        bundle.putParcelableArrayList(RegisterFilterActivity.KEY_SELECTED_FEED, fragment.list());
        data.putExtras(bundle);
        setResult(RESULT_OK, data);
        finish();
    }
}
