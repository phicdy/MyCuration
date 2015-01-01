package com.pluea.filfeed.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.pluea.filfeed.R;
import com.pluea.filfeed.alarm.AlarmManagerTaskManager;
import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.rss.Feed;
import com.pluea.filfeed.task.GetFeedIconTask;
import com.pluea.filfeed.task.InsertNewFeedTask;
import com.pluea.filfeed.task.UpdateTaskManager;

public class FeedListActivity extends Activity {

	private ArrayList<Feed> feeds = new ArrayList<Feed>();
	private DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(this);
	private PullToRefreshListView feedsListView;
	private TextView showNoUnread;
	private RssFeedListAdapter rssFeedListAdapter;
	private BroadcastReceiver receiver;
	private Intent intent;
	private UpdateTaskManager updateTaskManager;

	private static final int DELETE_FEED_MENU_ID = 0;
	private static final int EDIT_FEED_TITLE_MENU_ID = 1;
	
	public static final int BAD_RECIEVED_VALUE = -1;
	public static final String FEED_ID = "FEED_ID";
	public static final String FEED_URL = "FEED_URL";
	public static final String UPDATE_NUM_OF_ARTICLES = "UPDATE_NUM_OF_ARTICLES";
	public static final String FINISH_UPDATE_ACTION = "FINISH_UPDATE";
	private static final String LOG_TAG = "RSSREADER."
			+ FeedListActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed_list);

		feedsListView = (PullToRefreshListView) findViewById(R.id.feedList);
		updateTaskManager = UpdateTaskManager.getInstance(getApplicationContext());
		setAllListener();
		setAlarmManager();
		
		if(dbAdapter.getNumOfFeeds() == 0) {
			dbAdapter.addManyFeeds();
		}
		feeds = dbAdapter.getAllFeedsThatHaveUnreadArticles();
		getFeedIconIfNeeded(feeds);
		
		registerForContextMenu(feedsListView.getRefreshableView());
	}

	private void setAllListener() {
		// When an feed selected, display unread articles in the feed
		feedsListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						intent = new Intent(FeedListActivity.this,
								ArticlesListActivity.class);
						intent.putExtra(FEED_ID, feeds.get(position-1).getId());
						intent.putExtra(FEED_URL, feeds.get(position-1).getUrl());
						startActivity(intent);
					}

				});
		feedsListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				updateAllFeeds();
			}
		});
		
		TextView allUnread = (TextView)findViewById(R.id.allUnreadFeed);
		allUnread.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				intent = new Intent(FeedListActivity.this,
						ArticlesListActivity.class);
				startActivity(intent);
			}
		});
		
		showNoUnread = (TextView)findViewById(R.id.showNoUnread);
		showNoUnread.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				feeds = dbAdapter.getAllFeedsWithNumOfUnreadArticles();
				updateNumOfUnreadArticles(false);

				// Set ListView
				rssFeedListAdapter = new RssFeedListAdapter(feeds);
				feedsListView.setAdapter(rssFeedListAdapter);
			}
		});
	}

	private void setBroadCastReceiver() {
		// receive num of unread articles from Update Task
		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// Set num of unread articles and update UI
				if (intent.getAction().equals(FINISH_UPDATE_ACTION)) {
					Log.d(LOG_TAG, "onReceive");
					if (feedsListView.isRefreshing() && !updateTaskManager.isUpdating()) {
						feedsListView.onRefreshComplete();
						updateNumOfUnreadArticles(true);
					}
				}else if (intent.getAction().equals(UPDATE_NUM_OF_ARTICLES)) {
					updateNumOfUnreadArticles(true);
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(FINISH_UPDATE_ACTION);
		filter.addAction(UPDATE_NUM_OF_ARTICLES);
		registerReceiver(receiver, filter);

	}

	private void setAlarmManager() {
		// Start auto update alarmmanager
		AlarmManagerTaskManager.setNewAlarm(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.addFeed:
			addFeed();
			break;
		case R.id.addFilter:
			intent = new Intent(FeedListActivity.this, FilterList.class);
			startActivity(intent);
			break;
		case R.id.setting:
			startActivity(new Intent(getApplicationContext(), SettingActivity.class));
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);

		// Menu.add(int groupId, int itemId, int order, CharSequence title)
		menu.add(0, DELETE_FEED_MENU_ID, 0, R.string.delete_feed);
		menu.add(0, EDIT_FEED_TITLE_MENU_ID, 1, R.string.edit_feed_title);
	}

	public boolean onContextItemSelected(MenuItem item) {

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Feed selectedFeed = feeds.get(info.position-1);

		switch (item.getItemId()) {
		case DELETE_FEED_MENU_ID:
			showDeleteFeedAlertDialog(selectedFeed, info.position-1);
			return true;
		case EDIT_FEED_TITLE_MENU_ID:
			showEditTitleDialog(selectedFeed);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		setBroadCastReceiver();
//		updateAllFeeds();
		updateNumOfUnreadArticles(true);

		// Set ListView
		rssFeedListAdapter = new RssFeedListAdapter(feeds);
		feedsListView.setAdapter(rssFeedListAdapter);
		if (UpdateTaskManager.getInstance(getApplicationContext()).isUpdating()) {
			feedsListView.setRefreshing(true);
		}
	}
	
	@Override
	protected void onPause() {
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
		if (UpdateTaskManager.getInstance(getApplicationContext()).isUpdating()) {
			feedsListView.onRefreshComplete();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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

								InsertNewFeedTask task = new InsertNewFeedTask(
										getApplicationContext());
								task.execute(feedUrlStr);

								// Update feed list
								Feed newFeed;
								try {
									newFeed = task.get();
									if (newFeed == null) {
										Log.w("add a new feed",
												"Can't get feed url = "
														+ feedUrlStr);
										Toast.makeText(FeedListActivity.this,
												R.string.add_feed_error,
												Toast.LENGTH_SHORT).show();
									} else {
										// add new feed and notify to adapter
										feeds.add(newFeed);
										rssFeedListAdapter
												.notifyDataSetChanged();

										//TODO Don't show message
									}
								} catch (InterruptedException e) {
									e.printStackTrace();
								} catch (ExecutionException e) {
									e.printStackTrace();
								}

							}

						}).setNegativeButton(R.string.cancel, null).show();
	}

	private void showEditTitleDialog(final Feed selectedFeed) {
		final View addView = getLayoutInflater().inflate(R.layout.edit_feed_title, null);
		EditText editTitleView = (EditText) addView.findViewById(R.id.editFeedTitle);
		editTitleView.setText(selectedFeed.getTitle());

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
									int numOfUpdate = dbAdapter.saveNewTitle(selectedFeed.getId(), newTitle);
									if(numOfUpdate == 1) {
										Toast.makeText(getApplicationContext(), getString(R.string.edit_feed_title_success), Toast.LENGTH_SHORT).show();
										updateNumOfUnreadArticles(true);
									}else {
										Toast.makeText(getApplicationContext(), getString(R.string.edit_feed_title_error), Toast.LENGTH_SHORT).show();
									}
								}
							}

						}).setNegativeButton(R.string.cancel, null).show();
	}
	
	private void showDeleteFeedAlertDialog(final Feed selectedFeed, final int position) {
		new AlertDialog.Builder(this)
				.setTitle(R.string.delete_feed_alert)
				.setPositiveButton(R.string.delete,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(dbAdapter.deleteFeed(selectedFeed.getId())) {
									feeds.remove(position);
									rssFeedListAdapter.notifyDataSetChanged();
									Toast.makeText(getApplicationContext(), getString(R.string.finish_delete_feed_success), Toast.LENGTH_SHORT).show();
								}else {
									Toast.makeText(getApplicationContext(), getString(R.string.finish_delete_feed_fail), Toast.LENGTH_SHORT).show();
								}
							}

						}).setNegativeButton(R.string.cancel, null).show();
	}
	
	private void updateAllFeeds() {
		ArrayList<Feed> allFeeds = dbAdapter.getAllFeedsWithoutNumOfUnreadArticles();
		if (allFeeds == null || allFeeds.isEmpty()) {
			feedsListView.onRefreshComplete();
			return;
		} 

		// Get allFeeds from DB if other update task is not running
		if (updateTaskManager.updateAllFeeds(allFeeds)) {
		} else {
			feedsListView.onRefreshComplete();
		}
	}

	private void updateNumOfUnreadArticles(boolean isHide) {
		feeds = dbAdapter.getAllFeedsThatHaveUnreadArticles();
		if (feeds.isEmpty()) {
			return;
		}
		ArrayList<Feed> hideList = new ArrayList<Feed>(); 
		for (Feed feed : feeds) {
			int numOfUnreadArticles = feed.getUnreadAriticlesCount();
			if(numOfUnreadArticles == 0) {
				hideList.add(feed);
			}else {
				feed.setUnreadArticlesCount(numOfUnreadArticles);
			}
		}
		if(feeds.size() == hideList.size() || hideList.size() == 0) {
			showNoUnread.setVisibility(View.GONE);
		}else if(isHide) {
			showNoUnread.setVisibility(View.VISIBLE);
			for(Feed feed : hideList) {
				feeds.remove(feed);
			}
		}
		
		rssFeedListAdapter = new RssFeedListAdapter(feeds);
		feedsListView.setAdapter(rssFeedListAdapter);
		rssFeedListAdapter.notifyDataSetChanged();
	}

	private void getFeedIconIfNeeded(ArrayList<Feed> feeds) {
		for (Feed feed : feeds) {
			if(feed.getIconPath() == null || feed.getIconPath().equals(Feed.DEDAULT_ICON_PATH)) {
				GetFeedIconTask task = new GetFeedIconTask(getApplicationContext());
				task.execute(feed.getSiteUrl());
			}
		}
	}
	
	/**
	 * 
	 * @author kyamaguchi Display RSS Feeds List
	 */
	class RssFeedListAdapter extends ArrayAdapter<Feed> {
		public RssFeedListAdapter(ArrayList<Feed> feeds) {
			super(FeedListActivity.this, R.layout.feeds_list, feeds);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Use contentView
			View row = convertView;
			if (convertView == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.feeds_list, parent, false);
			}

			Feed feed = this.getItem(position);

			ImageView feedIcon = (ImageView)row.findViewById(R.id.feedIcon);
			String iconPath = feed.getIconPath();
			if(iconPath == null || iconPath.equals(Feed.DEDAULT_ICON_PATH)) {
				feedIcon.setImageResource(R.drawable.no_icon);
			}else {
				File file = new File(iconPath);
			    if (file.exists()) {
		            Bitmap bmp = BitmapFactory.decodeFile(file.getPath());
		            feedIcon.setImageBitmap(bmp); 
			    }
			}
			
			// set RSS Feed title
			TextView feedTitle = (TextView) row.findViewById(R.id.feedTitle);
			feedTitle.setText(feed.getTitle());

			// set RSS Feed unread article count
			TextView feedCount = (TextView) row.findViewById(R.id.feedCount);
			feedCount.setText(String.valueOf(feed.getUnreadAriticlesCount()));

			return (row);
		}

	}
}
