package com.phicdy.filfeed.ui;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.melnykov.fab.FloatingActionButton;
import com.phicdy.filfeed.R;
import com.phicdy.filfeed.db.DatabaseAdapter;
import com.phicdy.filfeed.rss.Article;
import com.phicdy.filfeed.rss.Feed;
import com.phicdy.filfeed.rss.UnreadCountManager;
import com.phicdy.filfeed.task.NetworkTaskManager;
import com.phicdy.filfeed.util.PreferenceHelper;
import com.phicdy.filfeed.util.TextUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class ArticlesListActivity extends ActionBarActivity {
    private ArrayList<Article> allArticles;
    private ArrayList<Article> loadedArticles = new ArrayList<>();
    private int feedId;
    private int curationId;
    private String feedUrl;
    private DatabaseAdapter dbAdapter;
    private PreferenceHelper prefMgr;
    private UnreadCountManager unreadManager;
    private Intent intent;

    private PullToRefreshListView articlesListView;
    private ArticlesListAdapter articlesListAdapter;
    private static final int SWIPE_MIN_WIDTH = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    public static final String OPEN_URL_ID = "openUrl";
    private static final int LOAD_COUNT = 100;
    private static final int DEFAULT_CURATION_ID = -1;
    private static final String LOAD_ARTICLE = "loadArticle";
    private static final String LOG_TAG = "FilFeed.ArticlesList";

    private GestureDetector mGestureDetector;
    private SimpleOnGestureListener mOnGestureListener;
    private boolean isSwipeRightToLeft = false;
    private boolean isSwipeLeftToRight = false;
    private int swipeDirectionOption = PreferenceHelper.SWIPE_DEFAULT;

    private SearchView searchView;
    private View footer;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles_list);

        dbAdapter  = DatabaseAdapter.getInstance(getApplicationContext());
        unreadManager = UnreadCountManager.getInstance(getApplicationContext());

        // Set feed id and url from main activity
        intent = getIntent();
        feedId = intent.getIntExtra(TopActivity.FEED_ID, Feed.ALL_FEED_ID);
        curationId = intent.getIntExtra(TopActivity.CURATION_ID, DEFAULT_CURATION_ID);
        intent.putExtra(TopActivity.FEED_ID, feedId);

        // Set swipe direction
        prefMgr = PreferenceHelper.getInstance(getApplicationContext());
        swipeDirectionOption = prefMgr.getSwipeDirection();

        // Set title
        if (curationId != DEFAULT_CURATION_ID) {
            // Curation
            setTitle(dbAdapter.getCurationNameById(curationId));
        }else if(feedId == Feed.ALL_FEED_ID) {
            // All article
            setTitle(getString(R.string.all));
        }else {
            // Select a feed
            prefMgr.setSearchFeedId(feedId);
            Feed selectedFeed = dbAdapter.getFeedById(feedId);
            feedUrl = selectedFeed.getUrl();
            setTitle(selectedFeed.getTitle());
        }
        setAllListener();
        getListView().setEmptyView(findViewById(R.id.emptyView));
        getListView().addFooterView(getFooter());
        displayUnreadArticles();
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
                if (feedId == Feed.ALL_FEED_ID) {
                    dbAdapter.saveAllStatusToRead();
                    unreadManager.readAll();
                }else {
                    dbAdapter.saveStatusToRead(feedId);
                    unreadManager.readAll(feedId);
                }
                if (prefMgr.getAllReadBack()) {
                    finish();
                }else {
                    for (Article article : allArticles) {
                        article.setStatus(Article.READ);
                    }
                    articlesListAdapter.notifyDataSetChanged();
                }
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
        invisibleFooter();
        setBroadCastReceiver();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private BroadcastReceiver loadArticleReceiver;

    private void setBroadCastReceiver() {
        // receive num of unread articles from Update Task
        loadArticleReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(LOAD_ARTICLE)) {
                    addArticlesToList();
                    getListView().invalidateViews();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(LOAD_ARTICLE);
        registerReceiver(loadArticleReceiver, filter);
    }

    private void setAllListener() {
        articlesListView = (PullToRefreshListView) findViewById(R.id.articleListRefresh);
        // When an article selected, open this URL in default browser
        getListView()
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        if(!isSwipeLeftToRight && !isSwipeRightToLeft) {
                            int touchedPosition = position - 1;
                            setReadStatusToTouchedView(touchedPosition, Article.TOREAD, false);
                            Article clickedArticle = loadedArticles.get(touchedPosition);
                            if(prefMgr.isOpenInternal()) {
                                intent = new Intent(getApplicationContext(), InternalWebViewActivity.class);
                                intent.putExtra(OPEN_URL_ID, clickedArticle.getUrl());
                            }else {
                                Uri uri = Uri.parse(clickedArticle.getUrl());
                                intent = new Intent(Intent.ACTION_VIEW, uri);
                            }
                            startActivity(intent);
                        }
                        isSwipeRightToLeft = false;
                        isSwipeLeftToRight = false;
                    }
                });
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                Log.d(LOG_TAG, "onLongClick");
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, loadedArticles.get(position - 1).getUrl());
                startActivity(intent);
                return true;
            }
        });
        getListView().setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });
        articlesListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                NetworkTaskManager networkTaskManager = NetworkTaskManager
                        .getInstance(getApplicationContext());
                // Update Feeds
                ArrayList<Feed> feeds = new ArrayList<Feed>();
                feeds.add(new Feed(feedId, null, feedUrl, "", "", 0));
                networkTaskManager.updateAllFeeds(feeds);
            }
        });

        articlesListView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount == firstVisibleItem + visibleItemCount) {
                    loadArticles();
                }
            }
        });

        // Handle swipe event
        mOnGestureListener = new SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent event1, MotionEvent event2,
                                   float velocityX, float velocityY) {
                isSwipeLeftToRight = false;
                isSwipeRightToLeft = false;
                try {
                    // Set touched position in articles list from touch event
                    int touchedPosition = getListView().pointToPosition(
                            (int) event1.getX(), (int) event1.getY()) -1;
                    if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH) {
                        return false;
                    }
                    // event1 is first motion event and event2 is second motion event.
                    // So, if the distance from event1'x to event2'x is longer than a certain value, it is swipe
                    // And if event1'x is bigger than event2'x, it is swipe from right
                    // And if event1'x is smaller than event2'x, it is swipe from left
                    if (event1.getX() - event2.getX() > SWIPE_MIN_WIDTH
                            && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        // Right to Left
                        isSwipeRightToLeft = true;
                        switch (swipeDirectionOption) {
                            case PreferenceHelper.SWIPE_RIGHT_TO_LEFT:
                                setReadStatusToTouchedView(touchedPosition, Article.TOREAD, prefMgr.getAllReadBack());
                                break;
                            case PreferenceHelper.SWIPE_LEFT_TO_RIGHT:
                                setReadStatusToTouchedView(touchedPosition, Article.UNREAD, prefMgr.getAllReadBack());
                                break;
                            default:
                                break;
                        }
                    } else if (event2.getX() - event1.getX() > SWIPE_MIN_WIDTH
                            && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        // Left to Right
                        isSwipeLeftToRight = true;
                        switch (swipeDirectionOption) {
                            case PreferenceHelper.SWIPE_RIGHT_TO_LEFT:
                                setReadStatusToTouchedView(touchedPosition, Article.UNREAD, prefMgr.getAllReadBack());
                                break;
                            case PreferenceHelper.SWIPE_LEFT_TO_RIGHT:
                                setReadStatusToTouchedView(touchedPosition, Article.TOREAD, prefMgr.getAllReadBack());
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                    // nothing
                }
                return false;
            }
        };
        mGestureDetector = new GestureDetector(this, mOnGestureListener);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
                    return;
                }
                ListView listView = getListView();
                int firstPosition = listView.getFirstVisiblePosition();
                int lastPosition = listView.getLastVisiblePosition();
                // Row in last visible position is hidden by buttons, don't change status
                for (int i = firstPosition; i < lastPosition - 1; i++) {
                    final Article targetArticle = loadedArticles.get(i);
                    if (targetArticle.getStatus().equals(Article.UNREAD)) {
                        targetArticle.setStatus(Article.TOREAD);
                        unreadManager.countDownUnreadCount(targetArticle.getFeedId());
                        dbAdapter.saveStatus(targetArticle.getId(), Article.TOREAD);
                    }
                }
                articlesListAdapter.notifyDataSetChanged();
                // Row in last visible position is hidden by buttons, so scroll to it
                getListView().smoothScrollToPositionFromTop(lastPosition, 4);
                if (prefMgr.getAllReadBack()) {
                    if (isAllRead()) {
                        finish();
                        return;
                    }
                }
            }
        });
    }
    private void displayUnreadArticles() {
        PreferenceHelper mgr = PreferenceHelper.getInstance(getApplicationContext());
        boolean isNewestArticleTop = mgr.getSortNewArticleTop();
        if (curationId != DEFAULT_CURATION_ID) {
            allArticles = dbAdapter.getAllUnreadArticlesOfCuration(curationId, isNewestArticleTop);
            if (allArticles.size() == 0) {
                allArticles = dbAdapter.getAllArticlesOfCuration(curationId, isNewestArticleTop);
            }
        }else if(feedId == Feed.ALL_FEED_ID) {
            allArticles = dbAdapter.getAllUnreadArticles(isNewestArticleTop);
            if(allArticles.size() == 0 && dbAdapter.isExistArticle()) {
                allArticles = dbAdapter.getTop300Articles(isNewestArticleTop);
            }
        }else {
            allArticles = dbAdapter.getUnreadArticlesInAFeed(feedId, isNewestArticleTop);
            if(allArticles.size() == 0 && dbAdapter.isExistArticle(feedId)) {
                allArticles = dbAdapter.getAllArticlesInAFeed(feedId, isNewestArticleTop);
            }
        }
        addArticlesToList();
        Log.d(LOG_TAG, "article size displayUnreadArticles():" + allArticles.size());
        articlesListAdapter = new ArticlesListAdapter(loadedArticles);
        articlesListView.setAdapter(articlesListAdapter);
    }

    private boolean isAllRead() {
        boolean isAllRead = true;
        for (Article article : loadedArticles) {
            if(article.getStatus().equals(Article.UNREAD)) {
                isAllRead = false;
                break;
            }
        }
        return  isAllRead;
    }

    private void setReadStatusToTouchedView(final int touchedPosition, final String status, boolean isAllReadBack) {
        final Article touchedArticle = loadedArticles.get(touchedPosition);
        String oldStatus = touchedArticle.getStatus();
        if (oldStatus.equals(status) || (oldStatus.equals(Article.READ) && status.equals(Article.TOREAD))) {
            return;
        }
        dbAdapter.saveStatus(touchedArticle.getId(), status);
        if (status.equals(Article.TOREAD)) {
            unreadManager.countDownUnreadCount(touchedArticle.getFeedId());
        }else if (status.equals(Article.UNREAD)) {
            unreadManager.conutUpUnreadCount(touchedArticle.getFeedId());
        }
        touchedArticle.setStatus(status);

        touchedArticle.setStatus(status);
        if(isAllReadBack) {
            if(isAllRead()) {
                finish();
            }
        }
        articlesListAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (loadArticleReceiver != null) {
            unregisterReceiver(loadArticleReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private AsyncTask<Long, Void, Void> mTask;

    private void loadArticles() {
        if (loadedArticles.size() == allArticles.size()) {
            // All articles are loaded
            invisibleFooter();
            fab.setVisibility(View.GONE);
            return;
        }

        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
            return;
        }

        getListView().addFooterView(getFooter());
        mTask = new AsyncTask<Long, Void, Void>() {
            @Override
            protected Void doInBackground(Long[] params) {
                try {
                    Thread.sleep(params[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            };

            protected void onPostExecute(Void result) {
                getApplicationContext().sendBroadcast(new Intent(LOAD_ARTICLE));
            };
        }.execute(Math.abs(new Random(System.currentTimeMillis()).nextLong() % 1000));

    }

    private void addArticlesToList() {
        int start = loadedArticles.size();
        for (int i = start; i < start + LOAD_COUNT; i++) {
            if (i >= allArticles.size()) {
                break;
            }
            loadedArticles.add(allArticles.get(i));
        }
    }

    private ListView getListView() {
        return articlesListView == null ? null : articlesListView.getRefreshableView();
    }

    private View getFooter() {
        if (footer == null) {
            footer = getLayoutInflater().inflate(R.layout.footer_article_list_activity,
                    null);
        }
        return footer;
    }

    private void invisibleFooter() {
        getListView().removeFooterView(getFooter());
    }

    /**
     *
     * @author kyamaguchi Display articles list
     */
    class ArticlesListAdapter extends ArrayAdapter<Article> {

        private ViewHolder holder;

        public ArticlesListAdapter(ArrayList<Article> articles) {
            /*
            * @param cotext
            *
            * @param int : Resource ID
            *
            * @param T[] objects : data list
            */
            super(ArticlesListActivity.this, R.layout.articles_list, articles);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Use contentView
            View row = convertView;
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.articles_list, parent, false);
                holder = new ViewHolder();
                holder.articleTitle = (TextView)row.findViewById(R.id.articleTitle);
                holder.articlePostedTime = (TextView) row.findViewById(R.id.articlePostedTime);
                holder.articlePoint = (TextView) row.findViewById(R.id.articlePoint);
                holder.feedTitleView = (TextView) row.findViewById(R.id.feedTitle);
                holder.feedIconView = (ImageView) row.findViewById(R.id.iv_feed_icon);
                row.setTag(holder);
            }else {
                holder = (ViewHolder)row.getTag();
            }
            
            Article article = this.getItem(position);
            if (article != null) {
                holder.articleTitle.setText(article.getTitle());

                // Set article posted date
                SimpleDateFormat format = new SimpleDateFormat(
                        "yyyy/MM/dd HH:mm:ss");
                String dateString = format.format(new Date(article
                        .getPostedDate()));
                holder.articlePostedTime.setText(dateString);

                // Set RSS Feed unread article count
                String hatenaPoint = article.getPoint();
                if(hatenaPoint.equals(Article.DEDAULT_HATENA_POINT)) {
                    holder.articlePoint.setText(getString(R.string.not_get_hatena_point));
                }else {
                    holder.articlePoint.setText(hatenaPoint);
                }

                String feedTitle = article.getFeedTitle();
                if(feedTitle == null) {
                    holder.feedTitleView.setVisibility(View.GONE);
                    holder.feedIconView.setVisibility(View.GONE);
                }else {
                    holder.feedTitleView.setText(feedTitle);
                    holder.feedTitleView.setTextColor(Color.BLACK);

                    String iconPath = article.getFeedIconPath();
                    if (!TextUtil.isEmpty(iconPath) && new File(iconPath).exists()) {
                        Bitmap bmp = BitmapFactory.decodeFile(article.getFeedIconPath());
                        holder.feedIconView.setImageBitmap(bmp);
                    }else {
                        holder.feedIconView.setImageResource(R.drawable.no_icon);
                    }
                }
                holder.articleTitle.setTextColor(Color.BLACK);
                holder.articlePostedTime.setTextColor(Color.BLACK);
                holder.articlePoint.setTextColor(Color.BLACK);

                // Change color if already be read
                if (article.getStatus().equals(Article.TOREAD) || article.getStatus().equals(Article.READ)) {
                    holder.articleTitle.setTextColor(Color.GRAY);
                    holder.articlePostedTime.setTextColor(Color.GRAY);
                    holder.articlePoint.setTextColor(Color.GRAY);
                    holder.feedTitleView.setTextColor(Color.GRAY);
                }
            }
            return row;
        }

        private class ViewHolder {
            TextView articleTitle;
            TextView articlePostedTime;
            TextView articlePoint;
            TextView feedTitleView;
            ImageView feedIconView;
        }
    }
}