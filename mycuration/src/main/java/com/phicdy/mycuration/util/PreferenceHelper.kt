package com.phicdy.mycuration.util

import android.content.Context
import android.content.SharedPreferences

object PreferenceHelper {
    private lateinit var pref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private const val PREF_KEY = "FilterPref"
    private const val KEY_AUTO_UPDATE_INTERVAL = "autoUpdateInterval"
    private const val KEY_AUTO_UPDATE_IN_MAIN_UI = "autoUpdateInMainUi"
    private const val KEY_SORT_NEW_ARTICLE_TOP = "sortNewArticleTop"
    private const val KEY_ALL_READ_BACK = "allReadBack"
    private const val KEY_SEARCH_FEED_ID = "searchFeedId"
    private const val KEY_OPEN_INTERNAL_ID = "openInternal"
    private const val KEY_SWIPE_DIRECTION = "swipeDirection"
    private const val KEY_LAUNCH_TAB = "launchTab"

    const val SWIPE_RIGHT_TO_LEFT = 0
    const val SWIPE_LEFT_TO_RIGHT = 1
    const val SWIPE_DEFAULT = SWIPE_RIGHT_TO_LEFT
    const val LAUNCH_CURATION = 0
    const val LAUNCH_RSS = 1
    const val LAUNCH_TAB_DEFAULT = LAUNCH_CURATION
    private val SWIPE_DIRECTIONS = intArrayOf(SWIPE_RIGHT_TO_LEFT, SWIPE_LEFT_TO_RIGHT)
    private val LAUNCH_TABS = intArrayOf(LAUNCH_CURATION, LAUNCH_RSS)
    private const val DEFAULT_UPDATE_INTERVAL_SECOND = 3 * 60 * 60

    fun setUp(context: Context) {
        pref = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
    }

    var autoUpdateIntervalSecond: Int
        get() = if (pref.contains(KEY_AUTO_UPDATE_INTERVAL)) {
            pref.getInt(KEY_AUTO_UPDATE_INTERVAL, DEFAULT_UPDATE_INTERVAL_SECOND)
        } else DEFAULT_UPDATE_INTERVAL_SECOND
        set(intervalSecond) {
            editor = pref.edit()
            editor.putInt(KEY_AUTO_UPDATE_INTERVAL, intervalSecond)
            editor.apply()
        }

    var autoUpdateInMainUi: Boolean
        get() = pref.getBoolean(KEY_AUTO_UPDATE_IN_MAIN_UI, false)
        set(isAutoUpdateInMainUi) {
            editor = pref.edit()
            editor.putBoolean(KEY_AUTO_UPDATE_IN_MAIN_UI, isAutoUpdateInMainUi)
            editor.apply()
        }

    var sortNewArticleTop: Boolean
        get() = !pref.contains(KEY_SORT_NEW_ARTICLE_TOP) || pref.getBoolean(KEY_SORT_NEW_ARTICLE_TOP, true)
        set(isNewArticleTop) {
            editor = pref.edit()
            editor.putBoolean(KEY_SORT_NEW_ARTICLE_TOP, isNewArticleTop)
            editor.apply()
        }

    var allReadBack: Boolean
        get() = !pref.contains(KEY_ALL_READ_BACK) || pref.getBoolean(KEY_ALL_READ_BACK, true)
        set(isAllReadBack) {
            editor = pref.edit()
            editor.putBoolean(KEY_ALL_READ_BACK, isAllReadBack)
            editor.apply()
        }

    var isOpenInternal: Boolean
        get() = pref.contains(KEY_OPEN_INTERNAL_ID) && pref.getBoolean(KEY_OPEN_INTERNAL_ID, false)
        set(isOpenInternal) {
            editor = pref.edit()
            editor.putBoolean(KEY_OPEN_INTERNAL_ID, isOpenInternal)
            editor.apply()
        }

    var swipeDirection: Int
        get() = if (pref.contains(KEY_SWIPE_DIRECTION)) {
            pref.getInt(KEY_SWIPE_DIRECTION, SWIPE_DEFAULT)
        } else SWIPE_DEFAULT
        set(newSwipeDirection) {
            for (direction in SWIPE_DIRECTIONS) {
                if (direction == newSwipeDirection) {
                    editor = pref.edit()
                    editor.putInt(KEY_SWIPE_DIRECTION, newSwipeDirection)
                    editor.apply()
                    return
                }
            }
        }

    var launchTab: Int
        get() = if (pref.contains(KEY_LAUNCH_TAB)) {
            pref.getInt(KEY_LAUNCH_TAB, LAUNCH_TAB_DEFAULT)
        } else LAUNCH_TAB_DEFAULT
        set(newLaunchTab) {
            for (tab in LAUNCH_TABS) {
                if (tab == newLaunchTab) {
                    editor = pref.edit()
                    editor.putInt(KEY_LAUNCH_TAB, newLaunchTab)
                    editor.apply()
                    return
                }
            }
        }

    fun setSearchFeedId(feedId: Int) {
        editor = pref.edit()
        editor.putInt(KEY_SEARCH_FEED_ID, feedId)
        editor.apply()
    }

}
