package com.phicdy.mycuration.presenter

import com.phicdy.mycuration.alarm.AlarmManagerTaskManager
import com.phicdy.mycuration.db.DatabaseAdapter
import com.phicdy.mycuration.util.PreferenceHelper
import com.phicdy.mycuration.view.SettingView

class SettingPresenter(private val helper: PreferenceHelper,
                       private val updateIntervalHourItems: Array<String>,
                       private val updateIntervalStringItems: Array<String>,
                       private val allReadBehaviorItems: Array<String>,
                       private val allReadBehaviorStringItems: Array<String>,
                       private val launchTabItems: Array<String>,
                       private val launchTabStringItems: Array<String>,
                       private val swipeDirectionItems: Array<String>,
                       private val swipeDirectionStringItems: Array<String>) : Presenter {
    private lateinit var view: SettingView

    fun setView(view: SettingView) {
        this.view = view
    }

    override fun create() {}

    override fun resume() {}

    override fun pause() {}

    fun activityCreate() {
        // Calc interval hour from saved interval second
        val autoUpdateIntervalSecond = helper.autoUpdateIntervalSecond
        val autoUpdateIntervalHour = autoUpdateIntervalSecond / (60 * 60)
        // Set index of saved interval
        for (i in updateIntervalHourItems.indices) {
            if (Integer.valueOf(updateIntervalHourItems[i]) == autoUpdateIntervalHour) {
                view.setUpdateInterval(i, updateIntervalStringItems[i])
                break
            }
        }

        view.setAutoUpdateInMainUi(helper.autoUpdateInMainUi)

        // Set default value of article sort option
        view.setArticleSort(helper.sortNewArticleTop)

        // Set default value of internal browser option
        view.setInternalBrowser(helper.isOpenInternal)

        // Set index of behavior of all read
        for (i in allReadBehaviorItems.indices) {
            val allBehaviorItemBool = Integer.valueOf(allReadBehaviorItems[i]) == 1
            val savedValue = helper.allReadBack
            if (allBehaviorItemBool == savedValue) {
                view.setAllReadBehavior(i, allReadBehaviorStringItems[i])
                break
            }
        }

        // Set index of swipe direction
        for (i in swipeDirectionItems.indices) {
            if (Integer.valueOf(swipeDirectionItems[i]) == helper.swipeDirection) {
                view.setSwipeDirection(i, swipeDirectionStringItems[i])
                break
            }
        }

        for (i in launchTabItems.indices) {
            if (Integer.valueOf(launchTabItems[i]) == helper.launchTab) {
                view.setLaunchTab(i, launchTabStringItems[i])
                break
            }
        }
    }

    fun updateUpdateInterval(intervalHour: Int,
                             manager: AlarmManagerTaskManager) {
        // Save new interval second
        val intervalSecond = intervalHour * 60 * 60
        helper.autoUpdateIntervalSecond = intervalSecond

        // Refresh summary
        for (i in updateIntervalHourItems.indices) {
            if (Integer.valueOf(updateIntervalHourItems[i]) == intervalHour) {
                view.setUpdateInterval(i, updateIntervalHourItems[i])
                break
            }
        }
        // Set new alarm
        manager.setNewAlarm(intervalSecond)
    }

    fun updateAllReadBehavior(isAllReadBack: Boolean) {
        // Save new behavior of all read
        helper.allReadBack = isAllReadBack

        // Refresh summary
        for (i in allReadBehaviorItems.indices) {
            val allBehaviorItemBool = Integer.valueOf(allReadBehaviorItems[i]) == 1
            if (allBehaviorItemBool == isAllReadBack) {
                view.setAllReadBehavior(i, allReadBehaviorStringItems[i])
                break
            }
        }

    }

    fun updateSwipeDirection(swipeDirection: Int) {
        // Save new swipe direction
        helper.swipeDirection = swipeDirection

        // Refresh summary
        for (i in swipeDirectionItems.indices) {
            if (Integer.valueOf(swipeDirectionItems[i]) == swipeDirection) {
                view.setSwipeDirection(i, swipeDirectionStringItems[i])
                break
            }
        }
    }

    fun updateArticleSort(isNewArticleTop: Boolean) {
        helper.sortNewArticleTop = isNewArticleTop
    }

    fun updateInternalBrowser(isInternal: Boolean) {
        helper.isOpenInternal = isInternal
    }

    fun updateAutoUpdateInMainUi(isEnaled: Boolean) {
        helper.autoUpdateInMainUi = isEnaled
    }

    fun updateLaunchTab(tab: Int) {
        helper.launchTab = tab

        // Refresh summary
        for (i in launchTabItems.indices) {
            if (Integer.valueOf(launchTabItems[i]) == tab) {
                view.setLaunchTab(i, launchTabStringItems[i])
                break
            }
        }
    }

    fun onLicenseClicked() {
        view.startLicenseActivity()
    }

    fun onDebugAddRssClicked(adapter: DatabaseAdapter) {
        adapter.saveNewFeed(
                "Yahoo!ニュース・トピックス - 主要",
                "https://news.yahoo.co.jp/pickup/rss.xml",
                "RSS2.0",
                "https://news.yahoo.co.jp")
        adapter.saveNewFeed(
                "Yahoo!ニュース・トピックス - 国際",
                "https://news.yahoo.co.jp/pickup/world/rss.xml",
                "RSS2.0",
                "https://news.yahoo.co.jp")
    }
}
