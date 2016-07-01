package com.phicdy.mycuration.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.alarm.AlarmManagerTaskManager;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.tracker.GATrackerHelper;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TopActivity extends ActionBarActivity implements FeedListFragment.OnFeedListFragmentListener, CurationListFragment.OnCurationListFragmentListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private DatabaseAdapter dbAdapter;
    private Intent intent;

    private CurationListFragment curationFragment;
    private SearchView searchView;
    private MyProgressDialogFragment progressDialog;
    private ViewGroup track;
    private HorizontalScrollView trackScroller;
    private View indicator;
    private static final int INDICATOR_OFFSET_DP = 48;
    private int indicatorOffset;
    private int selectedPosition = POSITION_FEED_FRAGMENT;

    private GATrackerHelper gaTrackerHelper;

    private static final int POSITION_CURATION_FRAGMENT = 0;
    private static final int POSITION_FEED_FRAGMENT = 1;
    private static final int POSITION_FILTER_FRAGMENT = 2;

    public static final String FEED_ID = "FEED_ID";
    public static final String CURATION_ID = "CURATION_ID";
    private static final String LOG_TAG = "FilFeed." + TopActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);

        curationFragment = new CurationListFragment();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new PageChangeListener());
        track = (ViewGroup)findViewById(R.id.track);
        trackScroller = (HorizontalScrollView)findViewById(R.id.track_scroller);
        indicator = (View)findViewById(R.id.indicator);

        WindowManager wm = getWindowManager();
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        int displayWidth = size.x;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            final int position = i;
            ImageView ivTab = (ImageView)inflater.inflate(R.layout.tab_item, track, false);
            ivTab.setImageResource(mSectionsPagerAdapter.getImageResource(position));
            ivTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(position);
                }
            });
            final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)ivTab.getLayoutParams();
            layoutParams.width = displayWidth / mSectionsPagerAdapter.getCount();
            ivTab.setLayoutParams(layoutParams);
            track.addView(ivTab);
        }

        final float density = getResources().getDisplayMetrics().density;
        indicatorOffset = (int)(INDICATOR_OFFSET_DP * density);

        setTitle(getString(R.string.home));

        dbAdapter = DatabaseAdapter.getInstance(getApplicationContext());
        setAlarmManager();

        gaTrackerHelper = GATrackerHelper.getInstance(this);
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
        searchAutoComplete.setTextColor(getResources().getColor(R.color.text_primary));
        searchAutoComplete.setHintTextColor(getResources().getColor(R.color.text_primary));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                switch (selectedPosition) {
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
    public void onCloseProgressDialog() {
        progressDialog.getDialog().dismiss();
    }

    @Override
    public void onCurationListClicked(int position) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), ArticlesListActivity.class);
        intent.putExtra(CURATION_ID, curationFragment.getCurationIdAtPosition(position));
        startActivity(intent);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
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

        public int getImageResource(int position) {
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

    private class PageChangeListener implements ViewPager.OnPageChangeListener {

        private int scrollState = ViewPager.SCROLL_STATE_IDLE;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            updateIndicatorPosition(position, positionOffset);
            setTitle(mSectionsPagerAdapter.getPageTitle(position));
        }

        @Override
        public void onPageSelected(int position) {
            if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
                updateIndicatorPosition(position, 0);
                setTitle(mSectionsPagerAdapter.getPageTitle(position));
            }
            gaTrackerHelper.sendScreen(mSectionsPagerAdapter.getPageTitle(position).toString());
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            scrollState = state;
        }

        private void updateIndicatorPosition(int position, float positionOffset) {
            final View currentView = track.getChildAt(position);
            final View nextView = position == (track.getChildCount() -1) ? null :
                    track.getChildAt(position + 1);
            int leftWidthOfCurrentView = currentView.getLeft();
            int currentWidth = currentView.getWidth();
            int nextWidth = nextView == null ? currentWidth : nextView.getWidth();

            int indicatorWidth = (int)(nextWidth * positionOffset +
                    currentWidth * (1 - positionOffset));
            int indicatorLeft = (int)(leftWidthOfCurrentView + positionOffset * currentWidth);

            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)indicator.getLayoutParams();
            layoutParams.width = indicatorWidth;
            layoutParams.setMargins(indicatorLeft, 0, 0, 0);
            indicator.setLayoutParams(layoutParams);

            trackScroller.scrollTo(indicatorLeft - indicatorOffset, 0);

            selectedPosition = position;
        }
    }
}
