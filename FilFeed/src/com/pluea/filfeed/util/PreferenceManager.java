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
	private static final String KEY_SWIPE_DIRECTION = "swipeDirection";
	
	public static final int SWIPE_RIGHT_TO_LEFT = 0;
	public static final int SWIPE_LEFT_TO_RIGHT = 1;
	public static final int SWIPE_DEFAULT = SWIPE_RIGHT_TO_LEFT;
	private static final int[] SWIPE_DIRECTIONS = {SWIPE_RIGHT_TO_LEFT, SWIPE_LEFT_TO_RIGHT};
	private static final int DEFAULT_UPDATE_INTERVAL_SECOND = 1 * 60 * 60;
	
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

	
	public int getAutoUpdateIntervalSecond() {
		if(pref.contains(KEY_AUTO_UPDATE_INTERVAL)) {
			return pref.getInt(KEY_AUTO_UPDATE_INTERVAL, DEFAULT_UPDATE_INTERVAL_SECOND);
		}
		return DEFAULT_UPDATE_INTERVAL_SECOND;
	}

	public void setAutoUpdateIntervalSecond(int intervalSecond) {
		editor = pref.edit();
		editor.putInt(KEY_AUTO_UPDATE_INTERVAL, intervalSecond);
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
	
	public int getSwipeDirection() {
		if(pref.contains(KEY_SWIPE_DIRECTION)) {
			return pref.getInt(KEY_SWIPE_DIRECTION, SWIPE_DEFAULT);
		}
		return SWIPE_DEFAULT;
	}
	
	public boolean setSwipeDirection(int newSwipeDirection) {
		for (int direction : SWIPE_DIRECTIONS) {
			if (direction == newSwipeDirection) {
				editor = pref.edit();
				editor.putInt(KEY_SWIPE_DIRECTION, newSwipeDirection);
				editor.commit();
				return true;
			}
		}
		return false;
	}
}
