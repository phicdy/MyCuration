package com.phicdy.mycuration.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class AlarmManagerTaskManager {

	private static final int HATENA_UPDATE_INTERVAL_AFTER_FEED_UPDATE_SECOND = 10;
    private final Context context;

    public AlarmManagerTaskManager(Context context) {
        this.context = context;
	}
	
	public void setNewAlarm(int intervalSec) {
		if (intervalSec == 0) {
			cancelAlarm(context, AutoUpdateBroadcastReciever.AUTO_UPDATE_ACTION);
			return;
		}
		setAlarm(context, AutoUpdateBroadcastReciever.AUTO_UPDATE_ACTION, intervalSec);
	}
	
	void setNewHatenaUpdateAlarmAfterFeedUpdate(Context context) {
		setAlarm(context, AutoUpdateBroadcastReciever.AUTO_UPDATE_HATENA_ACTION, HATENA_UPDATE_INTERVAL_AFTER_FEED_UPDATE_SECOND);
	}

	private void setAlarm(Context context, String action, int intervalSec) {
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

	private void cancelAlarm(Context context, String action) {
		Intent i = new Intent(context, AutoUpdateBroadcastReciever.class);
		i.setAction(action);

		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		AlarmManager alm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alm.cancel(pi);
	}
}
