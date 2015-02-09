package com.pluea.filfeed.ui;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.melnykov.fab.FloatingActionButton;
import com.pluea.filfeed.R;
import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.rss.Article;
import com.pluea.filfeed.rss.Feed;
import com.pluea.filfeed.task.UpdateTaskManager;
import com.pluea.filfeed.util.PreferenceManager;
public class ArticlesListActivity extends ActionBarActivity {
    private ArrayList<Article> articles;
    private int feedId;
    private String feedUrl;
    private DatabaseAdapter dbAdapter;
    private PreferenceManager prefMgr;
    private Intent intent;
    private PullToRefreshListView articlesListView;
    private ArticlesListAdapter articlesListAdapter;
    private int touchedPosition;
    private static final int SWIPE_MIN_WIDTH = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    public static final String OPEN_URL_ID = "openUrl";
    private static final String LOG_TAG = "RSSReader.ArticlesList";
    private GestureDetector mGestureDetector;
    private SimpleOnGestureListener mOnGestureListener;
    private boolean isSwipeRightToLeft = false;
    private boolean isSwipeLeftToRight = false;
    private int swipeDirectionOption = PreferenceManager.SWIPE_DEFAULT;
    private SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles_list);

        dbAdapter  = DatabaseAdapter.getInstance(getApplicationContext());

        // Set feed id and url from main activity
        intent = getIntent();
        feedId = intent.getIntExtra(FeedListActivity.FEED_ID, Feed.ALL_FEED_ID);
        feedUrl = intent.getStringExtra(FeedListActivity.FEED_URL);
        intent.putExtra(FeedListActivity.FEED_ID, feedId);
// intent.setAction(MainActivity.RECIEVE_UNREAD_CALC);
        prefMgr = PreferenceManager.getInstance(getApplicationContext());
        swipeDirectionOption = prefMgr.getSwipeDirection();
        if(feedId != Feed.ALL_FEED_ID) {
            prefMgr.setSearchFeedId(feedId);
// Init action bar
            Feed selectedFeed = dbAdapter.getFeedByUrl(feedUrl);
// title
            setTitle(selectedFeed.getTitle());
// icon
            String iconPath = selectedFeed.getIconPath();
            if(iconPath != null && !iconPath.equals(Feed.DEDAULT_ICON_PATH)) {
                File file = new File(iconPath);
                if (file.exists()) {
                    Drawable icon = Drawable.createFromPath(iconPath);
                    getSupportActionBar().setIcon(icon);
                }
            }
        }
        setAllListener();
        articlesListView.getRefreshableView().setEmptyView(findViewById(R.id.emptyView));
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
                if(!queryTextFocused) {
                    searchMenuItem.collapseActionView();
                    searchView.setQuery("", false);
                }
            }
        });
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (searchView != null) {
            searchView.onActionViewCollapsed();
            searchView.setQuery("",false);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }
    private void setAllListener() {
        articlesListView = (PullToRefreshListView) findViewById(R.id.articleListRefresh);
// When an article selected, open this URL in default browser
        articlesListView.getRefreshableView()
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        if(!isSwipeLeftToRight && !isSwipeRightToLeft) {
                            touchedPosition = position - 1;
                            setReadStatusToTouchedView(Color.GRAY, Article.TOREAD, false);
                            if(prefMgr.isOpenInternal()) {
                                intent = new Intent(getApplicationContext(), InternalWebViewActivity.class);
                                intent.putExtra(OPEN_URL_ID, articles.get(position-1).getUrl());
                            }else {
                                Uri uri = Uri
                                        .parse(articles.get(position-1).getUrl());
                                intent = new Intent(Intent.ACTION_VIEW, uri);
                            }
                            startActivity(intent);
                        }
                        isSwipeRightToLeft = false;
                        isSwipeLeftToRight = false;
                    }
                });
        articlesListView.getRefreshableView().setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                Log.d(LOG_TAG, "onLongClick");
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, articles.get(position-1).getUrl());
                startActivity(intent);
                return true;
            }
        });
        articlesListView.getRefreshableView().setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });
        articlesListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                UpdateTaskManager updateTaskManager = UpdateTaskManager
                        .getInstance(getApplicationContext());
// Update Feeds
                ArrayList<Feed> feeds = new ArrayList<Feed>();
                feeds.add(new Feed(feedId, null, feedUrl, "", "", 0));
                updateTaskManager.updateAllFeeds(feeds);
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
                    touchedPosition = articlesListView.getRefreshableView().pointToPosition(
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
                            case PreferenceManager.SWIPE_RIGHT_TO_LEFT:
                                setReadStatusToTouchedView(Color.GRAY, Article.TOREAD, prefMgr.getAllReadBack());
                                break;
                            case PreferenceManager.SWIPE_LEFT_TO_RIGHT:
                                setReadStatusToTouchedView(Color.BLACK, Article.UNREAD, prefMgr.getAllReadBack());
                                break;
                            default:
                                break;
                        }
                    } else if (event2.getX() - event1.getX() > SWIPE_MIN_WIDTH
                            && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
// Left to Right
                        isSwipeLeftToRight = true;
                        switch (swipeDirectionOption) {
                            case PreferenceManager.SWIPE_RIGHT_TO_LEFT:
                                setReadStatusToTouchedView(Color.BLACK, Article.UNREAD, prefMgr.getAllReadBack());
                                break;
                            case PreferenceManager.SWIPE_LEFT_TO_RIGHT:
                                setReadStatusToTouchedView(Color.GRAY, Article.TOREAD, prefMgr.getAllReadBack());
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

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView listView = articlesListView.getRefreshableView();
                int firstPosition = listView.getFirstVisiblePosition();
                int lastPosition = listView.getLastVisiblePosition();
// Row in last visible position is hidden by buttons, don't change status
                for (int i = firstPosition; i < lastPosition - 1; i++) {
                    final Article article = articles.get(i);
                   article.setStatus(Article.TOREAD);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dbAdapter.saveStatus(article.getId(), Article.TOREAD);
                        }
                    }).start();
                }
                articlesListAdapter.notifyDataSetChanged();
// Row in last visible position is hidden by buttons, so scroll to it
                articlesListView.getRefreshableView().smoothScrollToPositionFromTop(lastPosition, 4);
// Back option if all articles are read
                if(prefMgr.getAllReadBack()) {
                    boolean isAllRead = true;
                    for(Article article: articles) {
                        if(article.getStatus().equals(Article.UNREAD)) {
                            isAllRead = false;
                            break;
                        }
                    }
                    if(isAllRead) {
                        finish();
                    }
                }
            }
        });
    }
    private void displayUnreadArticles() {
        PreferenceManager mgr = PreferenceManager.getInstance(getApplicationContext());
        boolean isNewestArticleTop = mgr.getSortNewArticleTop();
        if(feedId == Feed.ALL_FEED_ID) {
            articles = dbAdapter.getAllUnreadArticles(isNewestArticleTop);
            if(articles.size() == 0 && dbAdapter.calcNumOfArticles() > 0) {
                articles = dbAdapter.getAllArticles(isNewestArticleTop);
            }
        }else {
            articles = dbAdapter.getUnreadArticlesInAFeed(feedId, isNewestArticleTop);
            if(articles.size() == 0 && dbAdapter.calcNumOfArticles(feedId) > 0) {
                articles = dbAdapter.getAllArticlesInAFeed(feedId, isNewestArticleTop);
            }
        }
        Log.d(LOG_TAG, "article size displayUnreadArticles():" + articles.size());
        articlesListAdapter = new ArticlesListAdapter(articles);
        articlesListView.setAdapter(articlesListAdapter);
    }
    private void setReadStatusToTouchedView(int color, final String status, boolean isAllReadBack) {
        View row = articlesListAdapter.getView(touchedPosition, null,
                articlesListView);
// Change selected article's view
        TextView title = (TextView) row.findViewById(R.id.articleTitle);
        TextView postedTime = (TextView) row
                .findViewById(R.id.articlePostedTime);
        TextView point = (TextView) row.findViewById(R.id.articlePoint);
        title.setTextColor(color);
        postedTime.setTextColor(color);
        point.setTextColor(color);
        Log.d(LOG_TAG, "touched article title:" + title.getText());
        for (final Article article : articles) {
            if (title.getText().equals(article.getTitle())) {
                Log.d(LOG_TAG, "touched article id:" + article.getId());
                article.setStatus(status);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dbAdapter.saveStatus(article.getId(), status);
                    }
                }).start();
                break;
            }
        }
        if(isAllReadBack) {
            boolean isAllRead = true;
            for (Article article : articles) {
                if(article.getStatus().equals(Article.UNREAD)) {
                    isAllRead = false;
                    break;
                }
            }
            if(isAllRead) {
                startActivity(new Intent(getApplicationContext(), FeedListActivity.class));
            }
        }
        articlesListAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onPause() {
        super.onPause();
// Change read status
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Article article : articles) {
                    if (article.getStatus().equals(Article.TOREAD)) {
                        Log.d(LOG_TAG, "save status, article title:" + article.getTitle());
                        dbAdapter.saveStatus(article.getId(), Article.READ);
                    }
                }
                Intent intent = new Intent(FeedListActivity.ACTION_UPDATE_NUM_OF_ARTICLES_NOW);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendBroadcast(intent);
            }
        }).start();
    }
    // If Back button pushed
    @Override
    protected void onDestroy() {
// onSaveInstanceState(readStatus);
        super.onDestroy();
    }
    /**
     *
     * @author kyamaguchi Display articles list
     */
    class ArticlesListAdapter extends ArrayAdapter<Article> {
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
            }
            Article article = this.getItem(position);
            if (article != null) {
// set RSS Feed title
                TextView articleTitle = (TextView) row
                        .findViewById(R.id.articleTitle);
                articleTitle.setText(article.getTitle());
// set RSS posted date
                TextView articlePostedTime = (TextView) row
                        .findViewById(R.id.articlePostedTime);
                SimpleDateFormat format = new SimpleDateFormat(
                        "yyyy/MM/dd HH:mm:ss");
                String dateString = format.format(new Date(article
                        .getPostedDate()));
                articlePostedTime.setText(dateString);
// set RSS Feed unread article count
                TextView articlePoint = (TextView) row
                        .findViewById(R.id.articlePoint);
                String hatenaPoint = article.getPoint();
                if(hatenaPoint.equals(Feed.DEDAULT_HATENA_POINT)) {
                    articlePoint.setText(getString(R.string.not_get_hatena_point));
                }else {
                    articlePoint.setText(hatenaPoint);
                }
                TextView feedTitleView = (TextView) row
                        .findViewById(R.id.feedTitle);
                String feedTitle = article.getFeedTitle();
                if(feedTitle == null) {
                    feedTitleView.setVisibility(View.GONE);
                }else {
                    feedTitleView.setText(feedTitle);
                }
                articleTitle.setTextColor(Color.BLACK);
                articlePostedTime.setTextColor(Color.BLACK);
                articlePoint.setTextColor(Color.BLACK);
                feedTitleView.setTextColor(Color.BLACK);
// If readStaus exists,change status
// if(readStatus.containsKey(String.valueOf(position)) &&
// readStatus.getInt(String.valueOf(position)) ==
// article.getId()) {
                if (article.getStatus().equals(Article.TOREAD) || article.getStatus().equals(Article.READ)) {
                    articleTitle.setTextColor(Color.GRAY);
                    articlePostedTime.setTextColor(Color.GRAY);
                    articlePoint.setTextColor(Color.GRAY);
                    feedTitleView.setTextColor(Color.GRAY);
                }
            }
            return (row);
        }
    }
}