package com.pluea.rssfilterreader.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.rssfilterreader.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.pluea.rssfilterreader.db.DatabaseAdapter;
import com.pluea.rssfilterreader.rss.Article;
import com.pluea.rssfilterreader.rss.Feed;
import com.pluea.rssfilterreader.task.UpdateTaskManager;
import com.pluea.rssfilterreader.util.PreferenceManager;

public class ArticlesListActivity extends ListActivity {

	private ArrayList<Article> articles;
	private static final int BAD_FEED_ID = -1;
	private int feedId;
	private String feedUrl;
	private DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
	private PreferenceManager prefMgr;
	private Intent intent;
	private BroadcastReceiver receiver;
	private PullToRefreshListView articlesListView;
	private ArticlesListAdapter articlesListAdapter;
	private ImageView updateView;
	private int touchedPosition;

	private static final int SWIPE_MIN_WIDTH = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private static final int NO_SWIPE_WIDTH = 10;
	private static final String LOG_TAG = "RSSReader.ArticlesList";

	private GestureDetector mGestureDetector;
	private SimpleOnGestureListener mOnGestureListener;
	private boolean isSwipeRightToLeft = false;
	private boolean isSwipeLeftToRight = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.articles_activity);

		// Set feed id and url from main activity
		intent = getIntent();
		feedId = intent.getIntExtra(FeedListActivity.FEED_ID, BAD_FEED_ID);
		feedUrl = intent.getStringExtra(FeedListActivity.FEED_URL);

		intent.putExtra(FeedListActivity.FEED_ID, feedId);
		// intent.setAction(MainActivity.RECIEVE_UNREAD_CALC);

		prefMgr = PreferenceManager.getInstance(getApplicationContext());
		
		setAllListener();
		articlesListView.getRefreshableView().setEmptyView(findViewById(R.id.emptyView));
		
		displayUnreadArticles();
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
							Uri uri = Uri
									.parse(articles.get(position-1).getUrl());
							intent = new Intent(Intent.ACTION_VIEW, uri);
							startActivity(intent);
						}
						isSwipeRightToLeft = false;
						isSwipeLeftToRight = false;
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
				feeds.add(new Feed(feedId, null, feedUrl, "", ""));
				updateTaskManager.updateAllFeeds(feeds);
			}
		});

		mOnGestureListener = new SimpleOnGestureListener() {
			@Override
			public boolean onFling(MotionEvent event1, MotionEvent event2,
					float velocityX, float velocityY) {
				isSwipeLeftToRight = false;
				isSwipeRightToLeft = false;
				try {
					
					touchedPosition = articlesListView.getRefreshableView().pointToPosition(
							(int) event1.getX(), (int) event1.getY()) -1;
					if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH) {
						return false;
					}

					if (event1.getX() - event2.getX() > SWIPE_MIN_WIDTH
							&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
						setReadStatusToTouchedView(Color.GRAY, Article.TOREAD, prefMgr.getAllReadBack());
						isSwipeLeftToRight = true;
					} else if (event2.getX() - event1.getX() > SWIPE_MIN_WIDTH
							&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
						setReadStatusToTouchedView(Color.BLACK, Article.UNREAD, prefMgr.getAllReadBack());
						isSwipeRightToLeft = true;
					}

				} catch (Exception e) {
					// nothing
				}
				return false;
			}

		};
		mGestureDetector = new GestureDetector(this, mOnGestureListener);
		
		Button allRealButton = (Button)findViewById(R.id.all_read);
		allRealButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for (Article article : articles) {
					article.setStatus(Article.TOREAD);
				}
				if(prefMgr.getAllReadBack()) {
					startActivity(new Intent(getApplicationContext(), FeedListActivity.class));
				}else {
					articlesListAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	private void displayUnreadArticles() {
		PreferenceManager mgr = PreferenceManager.getInstance(getApplicationContext());
		boolean isNewestArticleTop = mgr.getSortNewArticleTop();
		
		if(feedId == BAD_FEED_ID) {
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

	private void setReadStatusToTouchedView(int color, String status, boolean isAllReadBack) {
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
		for (Article article : articles) {
			if (title.getText().equals(article.getTitle())) {
				Log.d(LOG_TAG, "touched article id:" + article.getId());
				article.setStatus(status);
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
						dbAdapter.saveStatus(article.getId(), Article.TOREAD);
					}
				}
				Intent intent = new Intent(FeedListActivity.UPDATE_NUM_OF_ARTICLES);
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
				if(hatenaPoint.equals(DatabaseAdapter.DEDAULT_HATENA_POINT)) {
					articlePoint.setText(getString(R.string.not_get_hatena_point));
				}else {
					articlePoint.setText(hatenaPoint);
				}

				articleTitle.setTextColor(Color.BLACK);
				articlePostedTime.setTextColor(Color.BLACK);
				articlePoint.setTextColor(Color.BLACK);

				// If readStaus exists,change status
				// if(readStatus.containsKey(String.valueOf(position)) &&
				// readStatus.getInt(String.valueOf(position)) ==
				// article.getId()) {
				if (article.getStatus().equals(Article.TOREAD) || article.getStatus().equals(Article.READ)) {
					articleTitle.setTextColor(Color.GRAY);
					articlePostedTime.setTextColor(Color.GRAY);
					articlePoint.setTextColor(Color.GRAY);
				}
			}

			return (row);
		}

	}
}
