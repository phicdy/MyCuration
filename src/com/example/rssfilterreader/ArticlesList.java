package com.example.rssfilterreader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
import android.widget.Toast;

public class ArticlesList extends ListActivity {

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
		feedId = intent.getIntExtra(MainActivity.FEED_ID, BAD_FEED_ID);
		feedUrl = intent.getStringExtra(MainActivity.FEED_URL);

		intent.putExtra(MainActivity.FEED_ID, feedId);
		// intent.setAction(MainActivity.RECIEVE_UNREAD_CALC);

		setAllListener();

		setBroadCastReceiver();
		displayUnreadArticles();
		getAllArticlesHatenabookmark();
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
				UpdateAllFeedsTask updateTask = UpdateAllFeedsTask
						.getInstance();
				if (!updateTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
					// Update Feeds
					ArrayList<Feed> feeds = new ArrayList<Feed>();
					feeds.add(new Feed(feedId, null, feedUrl));
					updateTask.setActivity(ArticlesList.this);
					updateTask.setProgressVisibility(true);
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
						// 縦の移動距離が大きすぎる場合は無視
						return false;
					}

					if (event1.getX() - event2.getX() > SWIPE_MIN_WIDTH
							&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
						Toast.makeText(ArticlesList.this, "右から左",
								Toast.LENGTH_SHORT).show();
						setReadStatusToTouchedView(Color.BLACK, Article.UNREAD);
						isSwipeRightToLeft = true;
					} else if (event2.getX() - event1.getX() > SWIPE_MIN_WIDTH
							&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
						// 終了位置から開始位置の移動距離が指定値より大きい
						// X軸の移動速度が指定値より大きい
						Toast.makeText(ArticlesList.this, "左から右",
								Toast.LENGTH_SHORT).show();
						setReadStatusToTouchedView(Color.GRAY, Article.TOREAD);
						isSwipeLeftToRight = true;
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
				if(action.equals(MainActivity.UPDATE_NUM_OF_ARTICLES)) {
					displayUnreadArticles();
					articlesListAdapter.notifyDataSetChanged();
				}else if(action.equals(GetHatenaBookmarkPointTask.FINISH_GET_HATENA)) {
//					Log.d(LOG_TAG, "article size onRecieve():" + articles.size());
					int arrayIndex = intent.getIntExtra(GetHatenaBookmarkPointTask.ARTICLE_ARRAY_INDEX, 0);
					int articleId = intent.getIntExtra(GetHatenaBookmarkPointTask.ARTICLE_ID, 0);;
					String point = intent.getStringExtra(GetHatenaBookmarkPointTask.ARTICLE_POINT);
					Article article = articles.get(arrayIndex);
					// For reload before getting hatenabookmark point
					if(article.getId() == articleId) {
						article.setPoint(point);
						articlesListAdapter.notifyDataSetChanged();
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(MainActivity.UPDATE_NUM_OF_ARTICLES);
		filter.addAction(GetHatenaBookmarkPointTask.FINISH_GET_HATENA);
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

		// Change read status
		for (Article article : articles) {
			if (title.getText().equals(article.getTitle())) {
				dbAdapter.saveStatus(article.getId(), status);
				article.setStatus(status);
				break;
			}
		}
		articlesListAdapter.notifyDataSetChanged();
	}

	// If Back button pushed
	@Override
	protected void onDestroy() {
		// onSaveInstanceState(readStatus);
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	private void getAllArticlesHatenabookmark() {
		for(int i=0;i<articles.size();i++) {
			Article article = articles.get(i);
			article.setArrayIndex(i);
			GetHatenaBookmarkPointTask hatenaTask = new GetHatenaBookmarkPointTask(this);
			hatenaTask.execute(article);
		}
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
			super(ArticlesList.this, R.layout.articles_list, articles);
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
