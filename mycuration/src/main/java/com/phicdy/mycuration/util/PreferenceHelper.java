package com.phicdy.mycuration.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {

	private static final String PREF_KEY = "FilterPref";
	private static final String KEY_AUTO_UPDATE_INTERVAL = "autoUpdateInterval";
	private static final String KEY_AUTO_UPDATE_IN_MAIN_UI = "autoUpdateInMainUi";
	private static final String KEY_SORT_NEW_ARTICLE_TOP = "sortNewArticleTop";
	private static final String KEY_ALL_READ_BACK = "allReadBack";
	private static final String KEY_SEARCH_FEED_ID = "searchFeedId";
	private static final String KEY_OPEN_INTERNAL_ID = "openInternal";
	private static final String KEY_SWIPE_DIRECTION = "swipeDirection";
	
	public static final int SWIPE_RIGHT_TO_LEFT = 0;
	public static final int SWIPE_LEFT_TO_RIGHT = 1;
	private static final int SWIPE_DEFAULT = SWIPE_RIGHT_TO_LEFT;
	private static final int[] SWIPE_DIRECTIONS = {SWIPE_RIGHT_TO_LEFT, SWIPE_LEFT_TO_RIGHT};
	private static final int DEFAULT_UPDATE_INTERVAL_SECOND = 3 * 60 * 60;
	
	private static PreferenceHelper preMgr;
	private SharedPreferences pref = null;
	private SharedPreferences.Editor editor = null;

	private PreferenceHelper(Context context) {
		pref = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
	}

	public static PreferenceHelper getInstance(Context context) {
		if(preMgr == null) {
			preMgr = new PreferenceHelper(context);
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
		editor.apply();
	}

	public boolean getAutoUpdateInMainUi() {
		return !pref.contains(KEY_AUTO_UPDATE_IN_MAIN_UI) || pref.getBoolean(KEY_AUTO_UPDATE_IN_MAIN_UI, false);
	}

	public void setAutoUpdateInMainUi(boolean isAutoUpdateInMainUi) {
		editor = pref.edit();
		editor.putBoolean(KEY_AUTO_UPDATE_IN_MAIN_UI, isAutoUpdateInMainUi);
		editor.apply();
	}

	public boolean getSortNewArticleTop() {
		return !pref.contains(KEY_SORT_NEW_ARTICLE_TOP) || pref.getBoolean(KEY_SORT_NEW_ARTICLE_TOP, true);
	}

	public void setSortNewArticleTop(boolean isNewArticleTop) {
		editor = pref.edit();
		editor.putBoolean(KEY_SORT_NEW_ARTICLE_TOP, isNewArticleTop);
		editor.apply();
	}
	
	public boolean getAllReadBack() {
		return !pref.contains(KEY_ALL_READ_BACK) || pref.getBoolean(KEY_ALL_READ_BACK, true);
	}

	public void setAllReadBack(boolean isAllReadBack) {
		editor = pref.edit();
		editor.putBoolean(KEY_ALL_READ_BACK, isAllReadBack);
		editor.apply();
	}

	public void setSearchFeedId(int feedId) {
		editor = pref.edit();
		editor.putInt(KEY_SEARCH_FEED_ID, feedId);
		editor.apply();
	}
	
	public boolean isOpenInternal() {
		return pref.contains(KEY_OPEN_INTERNAL_ID) && pref.getBoolean(KEY_OPEN_INTERNAL_ID, false);
	}

	public void setOpenInternal(boolean isOpenInternal) {
		editor = pref.edit();
		editor.putBoolean(KEY_OPEN_INTERNAL_ID, isOpenInternal);
		editor.apply();
	}
	
	public int getSwipeDirection() {
		if(pref.contains(KEY_SWIPE_DIRECTION)) {
			return pref.getInt(KEY_SWIPE_DIRECTION, SWIPE_DEFAULT);
		}
		return SWIPE_DEFAULT;
	}
	
	public void setSwipeDirection(int newSwipeDirection) {
		for (int direction : SWIPE_DIRECTIONS) {
			if (direction == newSwipeDirection) {
				editor = pref.edit();
				editor.putInt(KEY_SWIPE_DIRECTION, newSwipeDirection);
				editor.apply();
                return;
			}
		}
	}
}
