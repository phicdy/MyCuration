package com.phicdy.filfeed.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import com.phicdy.filfeed.R;
import com.phicdy.filfeed.task.InsertNewFeedTask;

public class FeedUrlHookActivity extends Activity {

	private ProgressDialog dialog;
	private BroadcastReceiver receiver;

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
						if (action
								.equals(InsertNewFeedTask.FINISH_INSERT_SUCCEEDED)) {
							Toast.makeText(context, R.string.add_feed_success,
									Toast.LENGTH_SHORT).show();
							dialog.dismiss();
						} else if (action
								.equals(InsertNewFeedTask.FINISH_INSERT_FAILED)) {
							Toast.makeText(context, R.string.add_feed_error,
									Toast.LENGTH_SHORT).show();
							dialog.dismiss();
						}
						finish();
					}
				};
				IntentFilter filter = new IntentFilter();
				filter.addAction(InsertNewFeedTask.FINISH_INSERT_SUCCEEDED);
				filter.addAction(InsertNewFeedTask.FINISH_INSERT_FAILED);
				registerReceiver(receiver, filter);

				dialog.show();
				InsertNewFeedTask task = new InsertNewFeedTask(
						getApplicationContext());
				task.execute(url);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}

}
