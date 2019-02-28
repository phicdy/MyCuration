package com.phicdy.mycuration.presentation.presenter

import androidx.appcompat.app.AppCompatDelegate
import com.phicdy.mycuration.data.repository.AdditionalSettingApi
import com.phicdy.mycuration.domain.alarm.AlarmManagerTaskManager
import com.phicdy.mycuration.presentation.view.SettingView
import com.phicdy.mycuration.util.PreferenceHelper
import kotlinx.coroutines.coroutineScope
import java.io.File

class SettingPresenter(
        private val view: SettingView,
        private val helper: PreferenceHelper,
        private val addtionalSettingApi: AdditionalSettingApi,
        private val updateIntervalHourItems: Array<String>,
        private val updateIntervalStringItems: Array<String>,
        private val themeItems: Array<String>,
        private val themeStringItems: Array<String>,
        private val allReadBehaviorItems: Array<String>,
        private val allReadBehaviorStringItems: Array<String>,
        private val launchTabItems: Array<String>,
        private val launchTabStringItems: Array<String>,
        private val swipeDirectionItems: Array<String>,
        private val swipeDirectionStringItems: Array<String>
) : Presenter {

    override fun create() {}

    override fun resume() {}

    override fun pause() {}

    fun activityCreate() {
        view.initView()
        view.initListener()

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

        for (i in themeItems.indices) {
            if (Integer.valueOf(themeItems[i]) == helper.theme) {
                val mode = when (i) {
                    PreferenceHelper.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                    PreferenceHelper.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_NO
                }
                view.setTheme(i, themeStringItems[i], mode)
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

    fun updateTheme(theme: Int) {
        helper.theme = theme

        // Refresh summary
        for (i in themeItems.indices) {
            if (Integer.valueOf(themeItems[i]) == theme) {
                val mode = when (i) {
                    PreferenceHelper.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                    PreferenceHelper.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_NO
                }
                view.setTheme(i, themeStringItems[i], mode)
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

    suspend fun onDebugAddRssClicked() {
        addtionalSettingApi.addDebugRss()
    }

    suspend fun onImportDatabaseClicked(currentDb: File) = coroutineScope {
        addtionalSettingApi.importDb(currentDb)
    }

    suspend fun onExportDatabaseClicked(currentDb: File) = coroutineScope {
        addtionalSettingApi.exportDb(currentDb)
    }
}
