package com.pluea.rssfilterreader.alarm;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pleua.rssfilterreader.rss.Feed;
import com.pluea.rssfilterreader.db.DatabaseAdapter;
import com.pluea.rssfilterreader.task.UpdateTaskManager;

public class AutoUpdateBroadcastReciever extends BroadcastReceiver {

	public static final String AUTO_UPDATE_ACTION = "autoUpdateFeed";
	
	DatabaseAdapter dbAdapter;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("AutoUpdateBroadcastReciever", "onReceive");
		if(intent.getAction().equals(AUTO_UPDATE_ACTION)) {
			dbAdapter = new DatabaseAdapter(context);
			UpdateTaskManager updateTask = UpdateTaskManager.getInstance(context);
	
			updateTask.updateAllFeeds(dbAdapter.getAllFeeds());
			
			// Save new time
			AlarmManagerTaskManager.setNewAlarm(context);
		}
	}
}
