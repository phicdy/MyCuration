package com.phicdy.mycuration.data.preference

import android.content.Context
import android.content.SharedPreferences
import com.phicdy.mycuration.repository.BuildConfig

object PreferenceHelper {
    private lateinit var pref: SharedPreferences

    private const val PREF_KEY = "FilterPref"
    private const val KEY_AUTO_UPDATE_INTERVAL = "autoUpdateInterval"
    private const val KEY_AUTO_UPDATE_IN_MAIN_UI = "autoUpdateInMainUi"
    private const val KEY_THEME = "theme"
    private const val KEY_SORT_NEW_ARTICLE_TOP = "sortNewArticleTop"
    private const val KEY_ALL_READ_BACK = "allReadBack"
    private const val KEY_SEARCH_FEED_ID = "searchFeedId"
    private const val KEY_OPEN_INTERNAL_ID = "openInternal"
    private const val KEY_SWIPE_DIRECTION = "swipeDirection"
    private const val KEY_LAUNCH_TAB = "launchTab"
    private const val KEY_LAST_UPDATE_DATE = "lastUpdateDate"
    private const val KEY_REVIEW_COUNT = "reviewCount"
    private const val KEY_REVIEWED = "reviewed"

    const val SWIPE_RIGHT_TO_LEFT = 0
    const val SWIPE_LEFT_TO_RIGHT = 1
    const val SWIPE_DEFAULT = SWIPE_RIGHT_TO_LEFT
    const val LAUNCH_CURATION = 0
    const val LAUNCH_RSS = 1
    const val LAUNCH_TAB_DEFAULT = LAUNCH_CURATION
    const val THEME_LIGHT = 0
    const val THEME_DARK = 1
    const val THEME_DEFAULT = THEME_LIGHT
    private val THEMES = intArrayOf(THEME_LIGHT, THEME_DARK)
    private val SWIPE_DIRECTIONS = intArrayOf(SWIPE_RIGHT_TO_LEFT, SWIPE_LEFT_TO_RIGHT)
    private val LAUNCH_TABS = intArrayOf(LAUNCH_CURATION, LAUNCH_RSS)
    private const val DEFAULT_UPDATE_INTERVAL_SECOND = 3 * 60 * 60
    private val DEFAULT_REVIEW_COUNT = if (BuildConfig.DEBUG) 3 else 100

    fun setUp(context: Context) {
        pref = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
    }

    var autoUpdateIntervalSecond: Int
        get() = if (pref.contains(KEY_AUTO_UPDATE_INTERVAL)) {
            pref.getInt(KEY_AUTO_UPDATE_INTERVAL, DEFAULT_UPDATE_INTERVAL_SECOND)
        } else DEFAULT_UPDATE_INTERVAL_SECOND
        set(intervalSecond) {
            pref.put(KEY_AUTO_UPDATE_INTERVAL, intervalSecond)
        }

    var autoUpdateInMainUi: Boolean
        get() = pref.getBoolean(KEY_AUTO_UPDATE_IN_MAIN_UI, false)
        set(isAutoUpdateInMainUi) {
            pref.put(KEY_AUTO_UPDATE_IN_MAIN_UI, isAutoUpdateInMainUi)
        }

    var lastUpdateDate: Long
        get() = pref.getLong(KEY_LAST_UPDATE_DATE, 0)
        set(lastUpdateDate) {
            pref.put(KEY_LAST_UPDATE_DATE, lastUpdateDate)
        }

    var theme: Int
        get() = if (pref.contains(KEY_THEME)) {
            pref.getInt(KEY_THEME, THEME_DEFAULT)
        } else THEME_DEFAULT
        set(newTheme) {
            for (theme in THEMES) {
                if (theme == newTheme) {
                    pref.put(KEY_THEME, theme)
                }
            }
        }

    var sortNewArticleTop: Boolean
        get() = !pref.contains(KEY_SORT_NEW_ARTICLE_TOP) || pref.getBoolean(KEY_SORT_NEW_ARTICLE_TOP, true)
        set(isNewArticleTop) {
            pref.put(KEY_SORT_NEW_ARTICLE_TOP, isNewArticleTop)
        }

    var allReadBack: Boolean
        get() = !pref.contains(KEY_ALL_READ_BACK) || pref.getBoolean(KEY_ALL_READ_BACK, true)
        set(isAllReadBack) {
            pref.put(KEY_ALL_READ_BACK, isAllReadBack)
        }

    var isOpenInternal: Boolean
        get() = pref.contains(KEY_OPEN_INTERNAL_ID) && pref.getBoolean(KEY_OPEN_INTERNAL_ID, false)
        set(isOpenInternal) {
            pref.put(KEY_OPEN_INTERNAL_ID, isOpenInternal)
        }

    var swipeDirection: Int
        get() = if (pref.contains(KEY_SWIPE_DIRECTION)) {
            pref.getInt(KEY_SWIPE_DIRECTION, SWIPE_DEFAULT)
        } else SWIPE_DEFAULT
        set(newSwipeDirection) {
            for (direction in SWIPE_DIRECTIONS) {
                if (direction == newSwipeDirection) {
                    pref.put(KEY_SWIPE_DIRECTION, newSwipeDirection)
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
                    pref.put(KEY_LAUNCH_TAB, newLaunchTab)
                }
            }
        }

    fun setSearchFeedId(feedId: Int) {
        pref.put(KEY_SEARCH_FEED_ID, feedId)
    }

    fun getReviewCount(): Int = if (pref.contains(KEY_REVIEW_COUNT)) {
        pref.getInt(KEY_REVIEW_COUNT, DEFAULT_REVIEW_COUNT)
    } else DEFAULT_REVIEW_COUNT

    fun decreaseReviewCount() {
        pref.put(KEY_REVIEW_COUNT, getReviewCount() - 1)
    }

    fun resetReviewCount() {
        pref.put(KEY_REVIEW_COUNT, DEFAULT_REVIEW_COUNT)
    }

    fun isReviewed(): Boolean = if (pref.contains(KEY_REVIEWED)) {
        pref.getBoolean(KEY_REVIEWED, false)
    } else false

    fun setReviewed() {
        pref.put(KEY_REVIEWED, true)
    }

    private fun SharedPreferences.put(key: String, value: Int) {
        edit().putInt(key, value).apply()
    }

    private fun SharedPreferences.put(key: String, value: Long) {
        edit().putLong(key, value).apply()
    }

    private fun SharedPreferences.put(key: String, value: Boolean) {
        edit().putBoolean(key, value).apply()
    }
}
