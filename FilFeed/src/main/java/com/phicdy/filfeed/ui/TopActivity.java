package com.phicdy.filfeed.ui;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.phicdy.filfeed.R;
import com.phicdy.filfeed.alarm.AlarmManagerTaskManager;
import com.phicdy.filfeed.db.DatabaseAdapter;
import com.phicdy.filfeed.rss.Feed;
import com.phicdy.filfeed.task.NetworkTaskManager;

import java.util.ArrayList;

public class TopActivity extends ActionBarActivity implements FeedListFragment.OnFeedListFragmentListener{

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
    private BroadcastReceiver receiver;
    private Intent intent;
    private NetworkTaskManager networkTaskManager;

    private FeedListFragment listFragment;
    private SearchView searchView;
    private ViewGroup track;
    private HorizontalScrollView trackScroller;
    private View indicator;
    private static final int INDICATOR_OFFSET_DP = 48;
    private int indicatorOffset;

    private static final int DELETE_FEED_MENU_ID = 1000;
    private static final int EDIT_FEED_TITLE_MENU_ID = 1001;

    private boolean isForeground = true;

    public static final String FEED_ID = "FEED_ID";
    public static final String FEED_URL = "FEED_URL";
    public static final String FINISH_UPDATE_ACTION = "FINISH_UPDATE";
    private static final String LOG_TAG = "FilFeed." + TopActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);

        listFragment = new FeedListFragment();
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
        // Displayのインスタンス取得
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        int displayWidth = size.x;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            final int position = i;
            TextView tv = (TextView)inflater.inflate(R.layout.tab_item, track, false);
            tv.setText(mSectionsPagerAdapter.getPageTitle(position));
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(position);
                }
            });
            final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)tv.getLayoutParams();
            layoutParams.width = displayWidth / mSectionsPagerAdapter.getCount();
            tv.setLayoutParams(layoutParams);
            track.addView(tv);
        }

        final float density = getResources().getDisplayMetrics().density;
        indicatorOffset = (int)(INDICATOR_OFFSET_DP * density);

        setTitle(getString(R.string.home));

        dbAdapter = DatabaseAdapter.getInstance(getApplicationContext());
        networkTaskManager = NetworkTaskManager.getInstance(getApplicationContext());
        setAlarmManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeground = true;
        setBroadCastReceiver();

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
    }

    @Override
    protected void onPause() {
        isForeground = false;
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onPause();
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.allFeeds:
                listFragment.changeHideStatus();
                break;
            case R.id.addFeed:
                addFeed();
                break;
            case R.id.addFilter:
                intent = new Intent(getApplicationContext(), RegisterFilterActivity.class);
                startActivity(intent);
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
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);

        // Menu.add(int groupId, int itemId, int order, CharSequence title)
        menu.add(0, DELETE_FEED_MENU_ID, 0, R.string.delete_feed);
        menu.add(0, EDIT_FEED_TITLE_MENU_ID, 1, R.string.edit_feed_title);
    }

    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();

        switch (item.getItemId()) {
            case DELETE_FEED_MENU_ID:
                showDeleteFeedAlertDialog(info.position-1);
                return true;
            case EDIT_FEED_TITLE_MENU_ID:
                showEditTitleDialog(info.position-1);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void updateAllFeeds() {
        ArrayList<Feed> allFeeds = dbAdapter.getAllFeedsWithoutNumOfUnreadArticles();
        if (allFeeds == null || allFeeds.isEmpty()) {
            listFragment.onRefreshComplete();
            return;
        }

        networkTaskManager.updateAllFeeds(allFeeds);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK){
            finish();
            return true;
        }
        return false;
    }

    private void setBroadCastReceiver() {
        // receive num of unread articles from Update Task
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // Set num of unread articles and update UI
                String action = intent.getAction();
                if (action.equals(FINISH_UPDATE_ACTION)) {
                    Log.d(LOG_TAG, "onReceive");
                    if (networkTaskManager.isUpdatingFeed()) {
                        listFragment.updateProgress();
                    }else {
                        listFragment.onRefreshComplete();
                        listFragment.refreshList();
                    }
                }else if (action.equals(NetworkTaskManager.FINISH_ADD_FEED)) {
                    Feed newFeed = dbAdapter.getFeedByUrl(intent.getStringExtra(NetworkTaskManager.ADDED_FEED_URL));
                    if (newFeed == null) {
                        Toast.makeText(getApplicationContext(),
                                R.string.add_feed_error,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                R.string.add_feed_success,
                                Toast.LENGTH_SHORT).show();
                        listFragment.addFeed(newFeed);
                        listFragment.refreshList();
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(FINISH_UPDATE_ACTION);
        filter.addAction(NetworkTaskManager.FINISH_ADD_FEED);
        registerReceiver(receiver, filter);

    }

    private void addFeed() {
        final View addView = getLayoutInflater().inflate(R.layout.add_feed,
                null);

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_feed)
                .setView(addView)
                .setPositiveButton(R.string.register,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // Set feed URL and judge whether feed URL is
                                // RSS format
                                EditText feedUrl = (EditText) addView
                                        .findViewById(R.id.addFeedUrl);
                                String feedUrlStr = feedUrl.getText()
                                        .toString();
                                NetworkTaskManager.getInstance(getApplicationContext()).addNewFeed(feedUrlStr);
                            }

                        }).setNegativeButton(R.string.cancel, null).show();
    }

    private void showEditTitleDialog(final int position) {
        final View addView = getLayoutInflater().inflate(R.layout.edit_feed_title, null);
        EditText editTitleView = (EditText) addView.findViewById(R.id.editFeedTitle);
        editTitleView.setText(listFragment.getFeedTitleAtPosition(position));

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_feed_title)
                .setView(addView)
                .setPositiveButton(R.string.save,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText editTitleView = (EditText) addView
                                        .findViewById(R.id.editFeedTitle);
                                String newTitle = editTitleView.getText().toString();
                                if(newTitle == null || newTitle.equals("")) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.empty_title), Toast.LENGTH_SHORT).show();
                                }else {
                                    int numOfUpdate = dbAdapter.saveNewTitle(listFragment.getFeedIdAtPosition(position), newTitle);
                                    if(numOfUpdate == 1) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.edit_feed_title_success), Toast.LENGTH_SHORT).show();
                                        listFragment.refreshList();
                                    }else {
                                        Toast.makeText(getApplicationContext(), getString(R.string.edit_feed_title_error), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                        }).setNegativeButton(R.string.cancel, null).show();
    }

    private void showDeleteFeedAlertDialog(final int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_feed_alert)
                .setPositiveButton(R.string.delete,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(dbAdapter.deleteFeed(listFragment.getFeedIdAtPosition(position))) {
                                    listFragment.removeFeedAtPosition(position);
                                    Toast.makeText(getApplicationContext(), getString(R.string.finish_delete_feed_success), Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.finish_delete_feed_fail), Toast.LENGTH_SHORT).show();
                                }
                            }

                        }).setNegativeButton(R.string.cancel, null).show();
    }

    @Override
    public void onListClicked(int position) {
        intent = new Intent(getApplicationContext(),
                ArticlesListActivity.class);
        intent.putExtra(FEED_ID, listFragment.getFeedIdAtPosition(position));
        intent.putExtra(FEED_URL, listFragment.getFeedUrlAtPosition(position));
        startActivity(intent);
    }

    @Override
    public void onRefreshList() {
        updateAllFeeds();
    }

    @Override
    public void onAllUnreadClicked() {
        intent = new Intent(getApplicationContext(), ArticlesListActivity.class);
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
                case 0:
                    return listFragment;
                case 1:
                    return new FilterListFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.feed);
                case 1:
                    return getString(R.string.filter);
            }
            return null;
        }
    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {

        private int scrollState = ViewPager.SCROLL_STATE_IDLE;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            updateIndicatorPosition(position, positionOffset);
        }

        @Override
        public void onPageSelected(int position) {
            if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
                updateIndicatorPosition(position, 0);
            }
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
        }
    }
}
