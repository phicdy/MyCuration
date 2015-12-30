package com.phicdy.mycuration.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.task.NetworkTaskManager;
import com.phicdy.mycuration.tracker.GATrackerHelper;

public class FeedUrlHookActivity extends Activity {

	private ProgressDialog dialog;
	private BroadcastReceiver receiver;

	private GATrackerHelper gaTrackerHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed_url_hook);

		Intent intent = getIntent();
		if (intent != null && intent.getAction().equals(Intent.ACTION_VIEW)) {
			String url = intent.getDataString();
			if (url != null) {
				dialog = new ProgressDialog(this);
				dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				dialog.setMessage(getString(R.string.adding_feed));

				receiver = new BroadcastReceiver() {

					@Override
					public void onReceive(Context context, Intent intent) {
						String action = intent.getAction();
						if (action.equals(NetworkTaskManager.FINISH_ADD_FEED)) {
							DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(getApplicationContext());
							Feed newFeed = dbAdapter.getFeedByUrl(intent.getStringExtra(NetworkTaskManager.ADDED_FEED_URL));
							if (intent.hasExtra(NetworkTaskManager.ADD_FEED_ERROR_REASON) || newFeed == null) {
								int errorMessage = R.string.add_feed_error_generic;
								if (intent.getIntExtra(NetworkTaskManager.ADD_FEED_ERROR_REASON, -1)
										== NetworkTaskManager.ERROR_INVALID_URL) {
									errorMessage = R.string.add_feed_error_invalid_url;
								}
								Toast.makeText(getApplicationContext(),
										errorMessage,
										Toast.LENGTH_SHORT).show();
								gaTrackerHelper.sendEvent(getString(R.string.add_feed_from_intent_error));
							} else {
								Toast.makeText(getApplicationContext(),
										R.string.add_feed_success,
										Toast.LENGTH_SHORT).show();
							}
							dialog.dismiss();
							finish();
						}
					}
				};
				IntentFilter filter = new IntentFilter();
				filter.addAction(NetworkTaskManager.FINISH_ADD_FEED);
				registerReceiver(receiver, filter);

				dialog.show();
				NetworkTaskManager.getInstance(getApplicationContext()).addNewFeed(url);
			}
		}

		gaTrackerHelper = GATrackerHelper.getInstance(this);
		gaTrackerHelper.sendScreen(getString(R.string.add_feed_from_intent));
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}

}
