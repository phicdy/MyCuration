package com.pluea.rssfilterreader.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager extends Activity {

	private static final String PREF_KEY = "FilterPref";
	private static final String KEY_AUTO_UPDATE_INTERVAL = "autoUpdateInterval";
	public static int DEFAULT_VALUE = 0;
	private static PreferenceManager preMgr;
	private SharedPreferences pref = null;
	private SharedPreferences.Editor editor = null;

	private PreferenceManager(Context context) {
		pref = context.getSharedPreferences(PREF_KEY, MODE_PRIVATE);
	}

	public static PreferenceManager getInstance(Context context) {
		if(preMgr == null) {
			preMgr = new PreferenceManager(context);
		}
		return preMgr;
	}

	
	public int getAutoUpdateInterval() {
		if(pref.contains(KEY_AUTO_UPDATE_INTERVAL)) {
			return pref.getInt(KEY_AUTO_UPDATE_INTERVAL, DEFAULT_VALUE);
		}
		return DEFAULT_VALUE;
	}

	public void setAutoUpdateInterval(int interval) {
		editor = pref.edit();
		editor.putInt(KEY_AUTO_UPDATE_INTERVAL, interval);
		editor.commit();
	}
}
