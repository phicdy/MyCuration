package com.phicdy.mycuration.setting

import androidx.appcompat.app.AppCompatDelegate
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.AdditionalSettingApi
import com.phicdy.mycuration.domain.alarm.AlarmManagerTaskManager
import com.phicdy.mycuration.domain.setting.SettingInitialData
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class SettingPresenter @Inject constructor(
    private val view: SettingView,
    private val helper: PreferenceHelper,
    private val addtionalSettingApi: AdditionalSettingApi,
    private val settingInitialData: SettingInitialData
) {

    fun activityCreate() {
        view.initView()
        view.initListener()

        // Calc interval hour from saved interval second
        val autoUpdateIntervalSecond = helper.autoUpdateIntervalSecond
        val autoUpdateIntervalHour = autoUpdateIntervalSecond / (60 * 60)
        // Set index of saved interval
        for (i in settingInitialData.updateIntervalHourItems.indices) {
            if (Integer.valueOf(settingInitialData.updateIntervalHourItems[i]) == autoUpdateIntervalHour) {
                view.setUpdateInterval(i, settingInitialData.updateIntervalStringItems[i])
                break
            }
        }

        view.setAutoUpdateInMainUi(helper.autoUpdateInMainUi)

        // Set default value of article sort option
        view.setArticleSort(helper.sortNewArticleTop)

        // Set default value of internal browser option
        view.setInternalBrowser(helper.isOpenInternal)

        // Set index of behavior of all read
        for (i in settingInitialData.allReadBehaviorItems.indices) {
            val allBehaviorItemBool = Integer.valueOf(settingInitialData.allReadBehaviorItems[i]) == 1
            val savedValue = helper.allReadBack
            if (allBehaviorItemBool == savedValue) {
                view.setAllReadBehavior(i, settingInitialData.allReadBehaviorStringItems[i])
                break
            }
        }

        // Set index of swipe direction
        for (i in settingInitialData.swipeDirectionItems.indices) {
            if (Integer.valueOf(settingInitialData.swipeDirectionItems[i]) == helper.swipeDirection) {
                view.setSwipeDirection(i, settingInitialData.swipeDirectionStringItems[i])
                break
            }
        }

        for (i in settingInitialData.launchTabItems.indices) {
            if (Integer.valueOf(settingInitialData.launchTabItems[i]) == helper.launchTab) {
                view.setLaunchTab(i, settingInitialData.launchTabStringItems[i])
                break
            }
        }

        for (i in settingInitialData.themeItems.indices) {
            if (Integer.valueOf(settingInitialData.themeItems[i]) == helper.theme) {
                val mode = when (i) {
                    PreferenceHelper.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                    PreferenceHelper.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_NO
                }
                view.setTheme(i, settingInitialData.themeStringItems[i], mode)
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
        for (i in settingInitialData.updateIntervalHourItems.indices) {
            if (Integer.valueOf(settingInitialData.updateIntervalHourItems[i]) == intervalHour) {
                view.setUpdateInterval(i, settingInitialData.updateIntervalHourItems[i])
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
        for (i in settingInitialData.allReadBehaviorItems.indices) {
            val allBehaviorItemBool = Integer.valueOf(settingInitialData.allReadBehaviorItems[i]) == 1
            if (allBehaviorItemBool == isAllReadBack) {
                view.setAllReadBehavior(i, settingInitialData.allReadBehaviorStringItems[i])
                break
            }
        }

    }

    fun updateSwipeDirection(swipeDirection: Int) {
        // Save new swipe direction
        helper.swipeDirection = swipeDirection

        // Refresh summary
        for (i in settingInitialData.swipeDirectionItems.indices) {
            if (Integer.valueOf(settingInitialData.swipeDirectionItems[i]) == swipeDirection) {
                view.setSwipeDirection(i, settingInitialData.swipeDirectionStringItems[i])
                break
            }
        }
    }

    fun updateTheme(theme: Int) {
        helper.theme = theme

        // Refresh summary
        for (i in settingInitialData.themeItems.indices) {
            if (Integer.valueOf(settingInitialData.themeItems[i]) == theme) {
                val mode = when (i) {
                    PreferenceHelper.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                    PreferenceHelper.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_NO
                }
                view.setTheme(i, settingInitialData.themeStringItems[i], mode)
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
        for (i in settingInitialData.launchTabItems.indices) {
            if (Integer.valueOf(settingInitialData.launchTabItems[i]) == tab) {
                view.setLaunchTab(i, settingInitialData.launchTabStringItems[i])
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

    suspend fun onImportDatabaseSelected(currentDb: File, uri: InputStream) {
        addtionalSettingApi.importDb(currentDb, uri)
    }

    suspend fun onExportDatabaseClicked(currentDb: File) = coroutineScope {
        addtionalSettingApi.exportDb(currentDb)
    }

    suspend fun onFixUnreadCount() {
        addtionalSettingApi.fixUnreadCount()
    }
}
