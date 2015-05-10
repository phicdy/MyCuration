package com.pluea.filfeed.alarm;

import java.util.Calendar;

import com.pluea.filfeed.util.PreferenceManager;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmManagerTaskManager {

	private static final int HATENA_UPDATE_INTERVAL_AFTER_FEED_UPDATE_SECOND = 10;
	private static final int HATENA_UPDATE_INTERVAL_SECOND = 60 * 60 * 3;
	
	private AlarmManagerTaskManager() {
		
	}
	
	public static void setNewAlarm(Context context) {
		PreferenceManager mgr = PreferenceManager.getInstance(context);
		int intervalSec = mgr.getAutoUpdateIntervalSecond();
		if (intervalSec == 0) {
			cancelAlarm(context, AutoUpdateBroadcastReciever.AUTO_UPDATE_ACTION);
			return;
		}
		setAlarm(context, AutoUpdateBroadcastReciever.AUTO_UPDATE_ACTION, intervalSec);
	}
	
	public static void setNewHatenaUpdateAlarmAfterFeedUpdate(Context context) {
		setAlarm(context, AutoUpdateBroadcastReciever.AUTO_UPDATE_HATENA_ACTION, HATENA_UPDATE_INTERVAL_AFTER_FEED_UPDATE_SECOND);
	}

	private static void setAlarm(Context context, String action, int intervalSec) {
		Intent i = new Intent(context, AutoUpdateBroadcastReciever.class);
		i.setAction(action);
		
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		AlarmManager alm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, intervalSec);
		Log.d("AlarmManagerTaskManager", "Set alarm : " + calendar.getTime().toString());
		
		alm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
	}

	private static void cancelAlarm(Context context, String action) {
		Intent i = new Intent(context, AutoUpdateBroadcastReciever.class);
		i.setAction(action);

		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		AlarmManager alm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alm.cancel(pi);
	}
}
