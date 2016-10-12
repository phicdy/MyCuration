package com.phicdy.mycuration.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.alarm.AlarmManagerTaskManager;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.tracker.GATrackerHelper;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.deanwild.materialshowcaseview.IShowcaseListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class TopActivity extends AppCompatActivity implements FeedListFragment.OnFeedListFragmentListener, CurationListFragment.OnCurationListFragmentListener{

    private ViewPager mViewPager;

    private DatabaseAdapter dbAdapter;
    private Intent intent;

    private CurationListFragment curationFragment;
    private SearchView searchView;

    private GATrackerHelper gaTrackerHelper;

    private static final int POSITION_CURATION_FRAGMENT = 0;
    private static final int POSITION_FEED_FRAGMENT = 1;
    private static final int POSITION_FILTER_FRAGMENT = 2;

    public static final String FEED_ID = "FEED_ID";
    public static final String CURATION_ID = "CURATION_ID";

    private static final String SHOWCASE_ID = "tutorialAddRss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);

        curationFragment = new CurationListFragment();
        setTitle(getString(R.string.home));
        initViewPager();
        dbAdapter = DatabaseAdapter.getInstance(getApplicationContext());
        setAlarmManager();
        gaTrackerHelper = GATrackerHelper.getInstance(this);
    }

    private void initViewPager() {
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        // Set icon
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(mSectionsPagerAdapter.getImageResource(i));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                dbAdapter.saveAllStatusToReadFromToRead();
            }
        }).start();

        if (searchView != null) {
            searchView.onActionViewCollapsed();
            searchView.setQuery("",false);
        }
        gaTrackerHelper.sendScreen(getString(R.string.home));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
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
        SearchView.SearchAutoComplete searchAutoComplete =
                (SearchView.SearchAutoComplete) searchView
                .findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        searchAutoComplete.setHintTextColor(ContextCompat.getColor(this, R.color.text_primary));

        // Start tutorial at first time
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                View view = findViewById(R.id.add);
                new MaterialShowcaseView.Builder(TopActivity.this)
                        .setTarget(view)
                        .setContentText(
                                R.string.tutorial_go_to_search_rss_description)
                        .setDismissText(R.string.tutorial_next)
                        .singleUse(SHOWCASE_ID)
                        .setListener(new IShowcaseListener() {
                            @Override
                            public void onShowcaseDisplayed(MaterialShowcaseView materialShowcaseView) {

                            }

                            @Override
                            public void onShowcaseDismissed(MaterialShowcaseView materialShowcaseView) {
                                goToFeedSearch();
                            }
                        })
                        .show();
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                switch (mViewPager.getCurrentItem()) {
                    case POSITION_CURATION_FRAGMENT:
                        if (dbAdapter.getNumOfFeeds() == 0) {
                            goToFeedSearch();
                            break;
                        }
						Intent intent = new Intent(getApplicationContext(), AddCurationActivity.class);
						startActivity(intent);
                        gaTrackerHelper.sendEvent(getString(R.string.tap_add_curation));
                        break;
                    case POSITION_FEED_FRAGMENT:
                        goToFeedSearch();
                        break;
                    case POSITION_FILTER_FRAGMENT:
                        if (dbAdapter.getNumOfFeeds() == 0) {
                            goToFeedSearch();
                            break;
                        }
                        intent = new Intent(getApplicationContext(), RegisterFilterActivity.class);
                        startActivity(intent);
                        gaTrackerHelper.sendEvent(getString(R.string.tap_add_filter));
                        break;
                    default:
                }
                break;
            case R.id.setting:
                startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                break;
            case R.id.license:
                startActivity(new Intent(getApplicationContext(), LicenseActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setAlarmManager() {
        // Start auto update alarmmanager
        AlarmManagerTaskManager.setNewAlarm(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK){
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void goToFeedSearch() {
        gaTrackerHelper.sendEvent(getString(R.string.tap_add_rss));
        startActivity(new Intent(TopActivity.this, FeedSearchActivity.class));
    }

    @Override
    public void onListClicked(int feedId) {
        intent = new Intent(getApplicationContext(),
                ArticlesListActivity.class);
        intent.putExtra(FEED_ID, feedId);
        startActivity(intent);
    }

    @Override
    public void onAllUnreadClicked() {
        intent = new Intent(getApplicationContext(), ArticlesListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCurationListClicked(int position) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), ArticlesListActivity.class);
        intent.putExtra(CURATION_ID, curationFragment.getCurationIdAtPosition(position));
        startActivity(intent);
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case POSITION_CURATION_FRAGMENT:
                    return curationFragment;
                case POSITION_FEED_FRAGMENT:
                    return new FeedListFragment();
                case POSITION_FILTER_FRAGMENT:
                    return new FilterListFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case POSITION_CURATION_FRAGMENT:
                    return getString(R.string.curation);
                case POSITION_FEED_FRAGMENT:
                    return getString(R.string.rss);
                case POSITION_FILTER_FRAGMENT:
                    return getString(R.string.filter);
            }
            return null;
        }

        int getImageResource(int position) {
            switch (position) {
                case POSITION_CURATION_FRAGMENT:
                    return R.drawable.tab_coffee;
                case POSITION_FEED_FRAGMENT:
                    return R.drawable.tab_feed;
                case POSITION_FILTER_FRAGMENT:
                    return R.drawable.tab_filter;
            }
            return -1;
        }
    }
}
