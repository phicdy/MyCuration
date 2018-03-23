package com.phicdy.mycuration.presentation.view.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.data.rss.Feed;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.util.PreferenceHelper;
import com.phicdy.mycuration.presentation.view.fragment.ArticlesListFragment;

public class ArticlesListActivity extends AppCompatActivity implements ArticlesListFragment.OnArticlesListFragmentListener {

    public static final String OPEN_URL_ID = "openUrl";
    private static final int DEFAULT_CURATION_ID = -1;

    private SearchView searchView;

    private String gaTitle;

    private ArticlesListFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles_list);

        fragment = (ArticlesListFragment) getSupportFragmentManager().findFragmentById(R.id.fr_article_list);

        // Set feed id and url from main activity
        Intent intent = getIntent();
        int feedId = intent.getIntExtra(TopActivity.FEED_ID, Feed.ALL_FEED_ID);
        int curationId = intent.getIntExtra(TopActivity.CURATION_ID, DEFAULT_CURATION_ID);
        intent.putExtra(TopActivity.FEED_ID, feedId);

        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance();
        if (curationId != DEFAULT_CURATION_ID) {
            // Curation
            setTitle(dbAdapter.getCurationNameById(curationId));
            gaTitle = getString(R.string.curation);
        }else if(feedId == Feed.ALL_FEED_ID) {
            // All article
            setTitle(getString(R.string.all));
            gaTitle = getString(R.string.all);
        }else {
            // Select a feed
            PreferenceHelper prefMgr = PreferenceHelper.INSTANCE;
            prefMgr.setSearchFeedId(feedId);
            Feed selectedFeed = dbAdapter.getFeedById(feedId);
            setTitle(selectedFeed.getTitle());
            gaTitle = getString(R.string.ga_not_all_title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_article, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchMenuItem = menu.findItem(R.id.search_article);
        searchView = (SearchView)MenuItemCompat.getActionView(searchMenuItem);
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if (!queryTextFocused) {
                    searchMenuItem.collapseActionView();
                    searchView.setQuery("", false);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.all_read:
                GATrackerHelper.INSTANCE.sendEvent(getString(R.string.read_all_articles));
                fragment.handleAllRead();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchView != null) {
            searchView.onActionViewCollapsed();
            searchView.setQuery("",false);
        }
        GATrackerHelper.INSTANCE.sendScreen(gaTitle);
    }
}