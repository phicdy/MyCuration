package com.pluea.rssfilterreader.alarm;

import java.util.Calendar;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmManagerTaskManager {

	private static final int INTERVAL = 60*60*1;
	
	private AlarmManagerTaskManager() {
		
	}
	
	public static void setNewAlarm(Context context) {
		Intent i = new Intent(context, AutoUpdateBroadcastReciever.class);
		i.setAction(AutoUpdateBroadcastReciever.AUTO_UPDATE_ACTION);
		
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		AlarmManager alm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, INTERVAL);
		Log.d("AlarmManagerTaskManager", "Set alarm : " + calendar.getTime().toString());
		
		alm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
	}
}
