package com.phicdy.mycuration.presentation.view.activity;

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

import com.phicdy.mycuration.BuildConfig;
import com.phicdy.mycuration.R;
import com.phicdy.mycuration.alarm.AlarmManagerTaskManager;
import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.presentation.presenter.TopActivityPresenter;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.util.PreferenceHelper;
import com.phicdy.mycuration.presentation.view.TopActivityView;
import com.phicdy.mycuration.presentation.view.fragment.CurationListFragment;
import com.phicdy.mycuration.presentation.view.fragment.FeedListFragment;
import com.phicdy.mycuration.presentation.view.fragment.FilterListFragment;
import com.phicdy.mycuration.view.activity.SettingActivity;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.deanwild.materialshowcaseview.IShowcaseListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class TopActivity extends AppCompatActivity implements
        FeedListFragment.OnFeedListFragmentListener,
        CurationListFragment.OnCurationListFragmentListener,
        TopActivityView {

    private TopActivityPresenter presenter;
    private ViewPager mViewPager;

    private CurationListFragment curationFragment;
    private SearchView searchView;

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

        PreferenceHelper helper = PreferenceHelper.INSTANCE;
        presenter = new TopActivityPresenter(helper.getLaunchTab());
        presenter.setView(this);
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance();
        presenter.setDataAdapter(dbAdapter);
        presenter.create();
    }

    @Override
    public void initViewPager() {
        curationFragment = new CurationListFragment();
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                setActivityTitle(position);
            }

            @Override
            public void onPageSelected(int position) {
                setActivityTitle(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        // Set icon
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setIcon(mSectionsPagerAdapter.getImageResource(i));
            }
        }
    }

    private void setActivityTitle(int position) {
        switch (position) {
            case POSITION_CURATION_FRAGMENT:
                setTitle(getString(R.string.curation));
                break;
            case POSITION_FEED_FRAGMENT:
                setTitle(getString(R.string.rss));
                break;
            case POSITION_FILTER_FRAGMENT:
                setTitle(getString(R.string.filter));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchMenuItem = menu.findItem(R.id.search_article_top_activity);
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
        if (!BuildConfig.DEBUG) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    View view = findViewById(R.id.add_new_rss);
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
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        presenter.optionItemClicked(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setAlarmManager() {
        // Start auto update alarmmanager
        AlarmManagerTaskManager manager = new AlarmManagerTaskManager(this);
        PreferenceHelper helper = PreferenceHelper.INSTANCE;
        int intervalSec = helper.getAutoUpdateIntervalSecond();
        manager.setNewAlarm(intervalSec);
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

    @Override
    public void goToFeedSearch() {
        GATrackerHelper.INSTANCE.sendEvent(getString(R.string.tap_add_rss));
        startActivity(new Intent(TopActivity.this, FeedSearchActivity.class));
    }

    @Override
    public void goToAddCuration() {
        Intent intent = new Intent(getApplicationContext(), AddCurationActivity.class);
        startActivity(intent);
        GATrackerHelper.INSTANCE.sendEvent(getString(R.string.tap_add_curation));
    }

    @Override
    public void goToAddFilter() {
        Intent intent = new Intent(getApplicationContext(), RegisterFilterActivity.class);
        startActivity(intent);
        GATrackerHelper.INSTANCE.sendEvent(getString(R.string.tap_add_filter));
    }

    @Override
    public void goToSetting() {
        startActivity(new Intent(getApplicationContext(), SettingActivity.class));
    }

    @Override
    public int currentTabPosition() {
        return mViewPager.getCurrentItem();
    }

    @Override
    public void onListClicked(int feedId) {
        Intent intent = new Intent(getApplicationContext(), ArticlesListActivity.class);
        intent.putExtra(FEED_ID, feedId);
        startActivity(intent);
    }

    @Override
    public void onAllUnreadClicked() {
        Intent intent = new Intent(getApplicationContext(), ArticlesListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCurationListClicked(int curationId) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), ArticlesListActivity.class);
        intent.putExtra(CURATION_ID, curationId);
        startActivity(intent);
    }

    @Override
    public void closeSearchView() {
        if (searchView != null) {
            searchView.onActionViewCollapsed();
            searchView.setQuery("",false);
        }
    }

    @Override
    public void changeTab(int position) {
        if (position != POSITION_CURATION_FRAGMENT && position != POSITION_FEED_FRAGMENT &&
                position != POSITION_FILTER_FRAGMENT) return;
        mViewPager.setCurrentItem(position);
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

        int getImageResource(int position) {
            switch (position) {
                case POSITION_CURATION_FRAGMENT:
                    return R.drawable.tab_curation;
                case POSITION_FEED_FRAGMENT:
                    return R.drawable.tab_feed;
                case POSITION_FILTER_FRAGMENT:
                    return R.drawable.tab_filter;
            }
            return -1;
        }
    }
}
