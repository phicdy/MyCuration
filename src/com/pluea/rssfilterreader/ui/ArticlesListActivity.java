package com.pluea.rssfilterreader.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.example.rssfilterreader.R;
import com.pleua.rssfilterreader.rss.Article;
import com.pleua.rssfilterreader.rss.Feed;
import com.pluea.rssfilterreader.db.DatabaseAdapter;
import com.pluea.rssfilterreader.task.UpdateFeedsTask;

import android.R.id;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ArticlesListActivity extends ListActivity {

	private ArrayList<Article> articles;
	private static final int BAD_FEED_ID = -1;
	private int feedId;
	private String feedUrl;
	private DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
	private Intent intent;
	private BroadcastReceiver receiver;
	private ListView articlesListView;
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

		setAllListener();

		setBroadCastReceiver();
		displayUnreadArticles();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	private void setAllListener() {
		articlesListView = (ListView) findViewById(id.list);
		updateView = (ImageView) findViewById(R.id.update);

		// When an article selected, open this URL in default browser
		articlesListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						if(!isSwipeLeftToRight && !isSwipeRightToLeft) {
							touchedPosition = position;
							setReadStatusToTouchedView(Color.GRAY, Article.TOREAD);
							Uri uri = Uri
									.parse(articles.get(position).getUrl());
							intent = new Intent(Intent.ACTION_VIEW, uri);
							startActivity(intent);
						}
						isSwipeRightToLeft = false;
						isSwipeLeftToRight = false;
					}

				});

		articlesListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mGestureDetector.onTouchEvent(event);
			}

		});

		updateView.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				UpdateFeedsTask updateTask = UpdateFeedsTask
						.getInstance(getApplicationContext(), false);
				if (!updateTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
					// Update Feeds
					ArrayList<Feed> feeds = new ArrayList<Feed>();
					feeds.add(new Feed(feedId, null, feedUrl));
					updateTask.execute(feeds);
				}
			}
		});

		mOnGestureListener = new SimpleOnGestureListener() {
			@Override
			public boolean onFling(MotionEvent event1, MotionEvent event2,
					float velocityX, float velocityY) {
				isSwipeLeftToRight = false;
				isSwipeRightToLeft = false;
				try {
					touchedPosition = articlesListView.pointToPosition(
							(int) event1.getX(), (int) event1.getY());
					if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH) {
						return false;
					}

					if (event1.getX() - event2.getX() > SWIPE_MIN_WIDTH
							&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
						setReadStatusToTouchedView(Color.GRAY, Article.TOREAD);
						isSwipeLeftToRight = true;
					} else if (event2.getX() - event1.getX() > SWIPE_MIN_WIDTH
							&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
						setReadStatusToTouchedView(Color.BLACK, Article.UNREAD);
						isSwipeRightToLeft = true;
					}

				} catch (Exception e) {
					// nothing
				}
				return false;
			}

		};
		mGestureDetector = new GestureDetector(this, mOnGestureListener);
	}

	private void setBroadCastReceiver() {
		// receive from Update Task after update task finished
		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if(action.equals(FeedListActivity.UPDATE_NUM_OF_ARTICLES)) {
					displayUnreadArticles();
					articlesListAdapter.notifyDataSetChanged();
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(FeedListActivity.UPDATE_NUM_OF_ARTICLES);
		registerReceiver(receiver, filter);
	}

	private void displayUnreadArticles() {
		articles = dbAdapter.getUnreadArticlesInAFeed(feedId);
		Log.d(LOG_TAG, "article size displayUnreadArticles():" + articles.size());
		articlesListAdapter = new ArticlesListAdapter(articles);
		articlesListView.setAdapter(articlesListAdapter);
	}

	private void setReadStatusToTouchedView(int color, String status) {
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
		unregisterReceiver(receiver);
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
				articlePoint.setText(article.getPoint());

				articleTitle.setTextColor(Color.BLACK);
				articlePostedTime.setTextColor(Color.BLACK);
				articlePoint.setTextColor(Color.BLACK);

				// If readStaus exists,change status
				// if(readStatus.containsKey(String.valueOf(position)) &&
				// readStatus.getInt(String.valueOf(position)) ==
				// article.getId()) {
				if (article.getStatus().equals("toRead")) {
					articleTitle.setTextColor(Color.GRAY);
					articlePostedTime.setTextColor(Color.GRAY);
					articlePoint.setTextColor(Color.GRAY);
				}
			}

			return (row);
		}

	}
}
