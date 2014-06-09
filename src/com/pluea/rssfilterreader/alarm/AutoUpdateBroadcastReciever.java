package com.pluea.rssfilterreader.alarm;

import java.util.ArrayList;
import java.util.Calendar;

import com.pleua.rssfilterreader.rss.Feed;
import com.pluea.rssfilterreader.db.DatabaseAdapter;
import com.pluea.rssfilterreader.task.UpdateAllFeedsTask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class AutoUpdateBroadcastReciever extends BroadcastReceiver {

	DatabaseAdapter dbAdapter;
	private ArrayList<Feed> feeds = new ArrayList<Feed>();

	@Override
	public void onReceive(Context context, Intent intent) {
		dbAdapter = new DatabaseAdapter(context);
		UpdateAllFeedsTask updateTask = UpdateAllFeedsTask.getInstance();
		if (updateTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
			return;
		} else {
			feeds = dbAdapter.getAllFeeds();
			updateTask.setActivity(context);
			updateTask.setProgressVisibility(false);
			updateTask.execute(feeds);

			// Save new time
			AlarmManagerTaskManager.setNewAlarm(context);
		}
	}
}
