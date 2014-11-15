package com.pluea.filfeed.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager extends Activity {

	private static final String PREF_KEY = "FilterPref";
	private static final String KEY_AUTO_UPDATE_INTERVAL = "autoUpdateInterval";
	private static final String KEY_SORT_NEW_ARTICLE_TOP = "sortNewArticleTop";
	private static final String KEY_ALL_READ_BACK = "allReadBack";
	private static final String KEY_SEARCH_FEED_ID = "searchFeedId";
	private static final String KEY_OPEN_INTERNAL_ID = "openInternal";
	
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
	
	public boolean getSortNewArticleTop() {
		if(pref.contains(KEY_SORT_NEW_ARTICLE_TOP)) {
			return pref.getBoolean(KEY_SORT_NEW_ARTICLE_TOP, true);
		}
		return true;
	}

	public void setSortNewArticleTop(boolean isNewArticleTop) {
		editor = pref.edit();
		editor.putBoolean(KEY_SORT_NEW_ARTICLE_TOP, isNewArticleTop);
		editor.commit();
	}
	
	public boolean getAllReadBack() {
		if(pref.contains(KEY_ALL_READ_BACK)) {
			return pref.getBoolean(KEY_ALL_READ_BACK, true);
		}
		return true;
	}

	public void setAllReadBack(boolean isAllReadBack) {
		editor = pref.edit();
		editor.putBoolean(KEY_ALL_READ_BACK, isAllReadBack);
		editor.commit();
	}
	
	public int getSearchFeedId() {
		if(pref.contains(KEY_SEARCH_FEED_ID)) {
			return pref.getInt(KEY_SEARCH_FEED_ID, DEFAULT_VALUE);
		}
		return DEFAULT_VALUE;
	}

	public void setSearchFeedId(int feedId) {
		editor = pref.edit();
		editor.putInt(KEY_SEARCH_FEED_ID, feedId);
		editor.commit();
	}
	
	public boolean isOpenInternal() {
		if(pref.contains(KEY_OPEN_INTERNAL_ID)) {
			return pref.getBoolean(KEY_OPEN_INTERNAL_ID, false);
		}
		return false;
	}

	public void setOpenInternal(boolean isOpenInternal) {
		editor = pref.edit();
		editor.putBoolean(KEY_OPEN_INTERNAL_ID, isOpenInternal);
		editor.commit();
	}
}
