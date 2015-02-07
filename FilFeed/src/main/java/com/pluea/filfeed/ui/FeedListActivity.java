package com.pluea.filfeed.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pluea.filfeed.R;
import com.pluea.filfeed.alarm.AlarmManagerTaskManager;
import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.rss.Feed;
import com.pluea.filfeed.task.GetFeedIconTask;
import com.pluea.filfeed.task.InsertNewFeedTask;
import com.pluea.filfeed.task.UpdateTaskManager;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class FeedListActivity extends ActionBarActivity implements FeedListFragment.OnFeedListFragmentListener {

	private ArrayList<Feed> feeds = new ArrayList<>();
	private DatabaseAdapter dbAdapter;
	private BroadcastReceiver receiver;
	private Intent intent;
	private UpdateTaskManager updateTaskManager;

    private FeedListFragment listFragment;

	private static final int DELETE_FEED_MENU_ID = 0;
	private static final int EDIT_FEED_TITLE_MENU_ID = 1;
	
	public static final int BAD_RECIEVED_VALUE = -1;
	public static final String FEED_ID = "FEED_ID";
	public static final String FEED_URL = "FEED_URL";
	public static final String ACTION_UPDATE_NUM_OF_ARTICLES_NOW = "UPDATE_NUM_OF_ARTICLES";
	public static final String FINISH_UPDATE_ACTION = "FINISH_UPDATE";
	private static final String LOG_TAG = "RSSReader."
			+ FeedListActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed_list);

		dbAdapter = DatabaseAdapter.getInstance(getApplicationContext());
		updateTaskManager = UpdateTaskManager.getInstance(getApplicationContext());
		setAllListener();
		setAlarmManager();
		
		if(dbAdapter.getNumOfFeeds() == 0) {
			dbAdapter.addManyFeeds();
		}
		getFeedIconIfNeeded(feeds);
	}

	private void setAllListener() {
		LinearLayout allUnread = (LinearLayout)findViewById(R.id.ll_all_unread);
		allUnread.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				intent = new Intent(FeedListActivity.this,
						ArticlesListActivity.class);
				startActivity(intent);
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
					if (!updateTaskManager.isUpdatingFeed()) {
						listFragment.onRefreshComplete();
						listFragment.updateNumOfUnreadArticles();
					}
				}else if (intent.getAction().equals(ACTION_UPDATE_NUM_OF_ARTICLES_NOW)) {
					listFragment.updateNumOfUnreadArticles();
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(FINISH_UPDATE_ACTION);
		filter.addAction(ACTION_UPDATE_NUM_OF_ARTICLES_NOW);
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
			intent = new Intent(FeedListActivity.this, FilterListActivity.class);
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

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, new FeedUpdateProgressFragment());
        transaction.commit();

        feeds = dbAdapter.getAllFeedsThatHaveUnreadArticles();
        listFragment = new FeedListFragment(feeds);
        FragmentTransaction listReplaceTransaction = manager.beginTransaction();
        listReplaceTransaction.replace(R.id.container, listFragment);
        listReplaceTransaction.commit();

	}
	
	@Override
	protected void onPause() {
		if (receiver != null) {
			unregisterReceiver(receiver);
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

										listFragment.addFeed(newFeed);

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
										listFragment.updateNumOfUnreadArticles();
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
                                    listFragment.removeFeedAtPosition(position);
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
			listFragment.onRefreshComplete();
			return;
		} 

		updateTaskManager.updateAllFeeds(allFeeds);
	}

	private void getFeedIconIfNeeded(ArrayList<Feed> feeds) {
		for (Feed feed : feeds) {
			if(feed.getIconPath() == null || feed.getIconPath().equals(Feed.DEDAULT_ICON_PATH)) {
				GetFeedIconTask task = new GetFeedIconTask(getApplicationContext());
				task.execute(feed.getSiteUrl());
			}
		}
	}

    @Override
    public void onListClicked(int position) {
        intent = new Intent(FeedListActivity.this,
                ArticlesListActivity.class);
        intent.putExtra(FEED_ID, feeds.get(position).getId());
        intent.putExtra(FEED_URL, feeds.get(position).getUrl());
        startActivity(intent);
    }

    @Override
    public void onRefreshList() {
        updateAllFeeds();
    }
}
