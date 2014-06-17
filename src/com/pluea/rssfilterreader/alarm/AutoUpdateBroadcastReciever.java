package com.pluea.rssfilterreader.alarm;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.pleua.rssfilterreader.rss.Feed;
import com.pluea.rssfilterreader.db.DatabaseAdapter;
import com.pluea.rssfilterreader.task.UpdateAllFeedsTask;

public class AutoUpdateBroadcastReciever extends BroadcastReceiver {

	DatabaseAdapter dbAdapter;
	private ArrayList<Feed> feeds = new ArrayList<Feed>();

	@Override
	public void onReceive(Context context, Intent intent) {
		dbAdapter = new DatabaseAdapter(context);
		UpdateAllFeedsTask updateTask = UpdateAllFeedsTask.getInstance(context, false);
		if (updateTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
			return;
		} else {
			feeds = dbAdapter.getAllFeeds();
			updateTask.execute(feeds);

			// Save new time
			AlarmManagerTaskManager.setNewAlarm(context);
		}
	}
}
