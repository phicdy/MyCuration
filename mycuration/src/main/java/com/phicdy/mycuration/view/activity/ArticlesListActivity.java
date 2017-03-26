package com.phicdy.mycuration.view.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.view.fragment.ArticlesListFragment;
import com.phicdy.mycuration.util.PreferenceHelper;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ArticlesListActivity extends AppCompatActivity implements ArticlesListFragment.OnArticlesListFragmentListener {

    public static final String OPEN_URL_ID = "openUrl";
    private static final int DEFAULT_CURATION_ID = -1;

    private GestureDetector mGestureDetector;

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

        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(getApplicationContext());
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
            PreferenceHelper prefMgr = PreferenceHelper.getInstance(getApplicationContext());
            prefMgr.setSearchFeedId(feedId);
            Feed selectedFeed = dbAdapter.getFeedById(feedId);
            setTitle(selectedFeed.getTitle());
            getString(R.string.ga_not_all_title);
        }
        setAllListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_article, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchMenuItem = menu.findItem(R.id.search);
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
                GATrackerHelper.sendEvent(getString(R.string.read_all_articles));
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
        GATrackerHelper.sendScreen(gaTitle);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setAllListener() {
        // Handle swipe event
        SimpleOnGestureListener mOnGestureListener = new SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent event1, MotionEvent event2,
                                   float velocityX, float velocityY) {
                fragment.onFlying(event1, event2, velocityX);
                return true;
            }
        };
        mGestureDetector = new GestureDetector(this, mOnGestureListener);
    }

    @Override
    public boolean onListViewTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }
}