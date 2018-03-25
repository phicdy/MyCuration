package com.phicdy.mycuration.presentation.view.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.widget.Toast

import com.phicdy.mycuration.BuildConfig
import com.phicdy.mycuration.R
import com.phicdy.mycuration.domain.alarm.AlarmManagerTaskManager
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.presentation.presenter.SettingPresenter
import com.phicdy.mycuration.tracker.GATrackerHelper
import com.phicdy.mycuration.util.PreferenceHelper
import com.phicdy.mycuration.util.ToastHelper
import com.phicdy.mycuration.presentation.view.SettingView


class SettingFragment : PreferenceFragment(), SettingView {
    private lateinit var presenter: SettingPresenter

    private lateinit var prefUpdateInterval: ListPreference
    private lateinit var prefLaunchTab: ListPreference
    private lateinit var prefAllReadBehavior: ListPreference
    private lateinit var prefSwipeDirection: ListPreference
    private lateinit var prefAutoUpdateInMainUi: SwitchPreference
    private lateinit var prefArticleSort: SwitchPreference
    private lateinit var prefInternalBrowser: SwitchPreference
    private lateinit var prefLicense: Preference

    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            addPreferencesFromResource(R.xml.setting_fragment_debug)
        } else {
            addPreferencesFromResource(R.xml.setting_fragment)
        }
        val helper = PreferenceHelper
        val updateIntervalHourItems = resources.getStringArray(R.array.update_interval_items_values)
        val updateIntervalStringItems = resources.getStringArray(R.array.update_interval_items)
        val allReadBehaviorItems = resources.getStringArray(R.array.all_read_behavior_values)
        val allReadBehaviorStringItems = resources.getStringArray(R.array.all_read_behavior)
        val launchTabItems = resources.getStringArray(R.array.launch_tab_items_values)
        val launchTabStringItems = resources.getStringArray(R.array.launch_tab_items)
        val swipeDirectionItems = resources.getStringArray(R.array.swipe_direction_items_values)
        val swipeDirectionStringItems = resources.getStringArray(R.array.swipe_direction_items)
        presenter = SettingPresenter(helper, updateIntervalHourItems,
                updateIntervalStringItems, allReadBehaviorItems, allReadBehaviorStringItems,
                launchTabItems, launchTabStringItems,
                swipeDirectionItems, swipeDirectionStringItems)
        presenter.setView(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.activityCreate()
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    override fun initView() {
        prefUpdateInterval = findPreference(getString(R.string.key_update_interval)) as ListPreference
        prefAutoUpdateInMainUi = findPreference(getString(R.string.key_auto_update_in_main_ui)) as SwitchPreference
        prefArticleSort = findPreference(getString(R.string.key_article_sort)) as SwitchPreference
        prefInternalBrowser = findPreference(getString(R.string.key_internal_browser)) as SwitchPreference
        prefAllReadBehavior = findPreference(getString(R.string.key_all_read_behavior)) as ListPreference
        prefSwipeDirection = findPreference(getString(R.string.key_swipe_direction)) as ListPreference
        prefLaunchTab = findPreference(getString(R.string.key_launch_tab)) as ListPreference
        prefLicense = findPreference(getString(R.string.key_license))
    }

    override fun initListener() {
        listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                getString(R.string.key_update_interval) -> {
                    val intervalHour = Integer.valueOf(prefUpdateInterval.value)
                    val manager = AlarmManagerTaskManager(activity)
                    presenter.updateUpdateInterval(intervalHour, manager)
                    // GA
                    GATrackerHelper.sendEvent(getString(R.string.change_auto_update_interval), intervalHour.toLong())
                }
                getString(R.string.key_all_read_behavior) -> {
                    val isAllReadBack = Integer.valueOf(prefAllReadBehavior.value) == 1
                    presenter.updateAllReadBehavior(isAllReadBack)
                    // GA
                    GATrackerHelper.sendEvent(getString(R.string.change_all_read_behavior), (if (isAllReadBack) 1 else 0).toLong())
                }
                getString(R.string.key_swipe_direction) -> {
                    val swipeDirection = Integer.valueOf(prefSwipeDirection.value)
                    presenter.updateSwipeDirection(swipeDirection)
                    // GA
                    GATrackerHelper.sendEvent(getString(R.string.change_swipe_direction), swipeDirection.toLong())
                }
                getString(R.string.key_article_sort) -> {
                    val isNewArticleTop = prefArticleSort.isChecked
                    presenter.updateArticleSort(isNewArticleTop)
                    // GA
                    GATrackerHelper.sendEvent(getString(R.string.change_aricle_sort), (if (prefArticleSort.isChecked) 1 else 0).toLong())
                }
                getString(R.string.key_internal_browser) -> {
                    val isInternal = prefInternalBrowser.isChecked
                    presenter.updateInternalBrowser(isInternal)
                    // GA
                    GATrackerHelper.sendEvent(getString(R.string.change_browser_option), (if (prefInternalBrowser.isChecked) 1 else 0).toLong())
                }
                getString(R.string.key_auto_update_in_main_ui) -> {
                    val isAutoUpdateInMainUi = prefAutoUpdateInMainUi.isChecked
                    presenter.updateAutoUpdateInMainUi(isAutoUpdateInMainUi)
                    GATrackerHelper.sendEvent(getString(R.string.change_auto_update_in_main_ui_option), (if (isAutoUpdateInMainUi) 1 else 0).toLong())
                }
                getString(R.string.key_launch_tab) -> {
                    val launchTab = Integer.valueOf(prefLaunchTab.value)
                    presenter.updateLaunchTab(launchTab)
                }
            }
        }

        if (BuildConfig.DEBUG) {
            val prefImport = findPreference(getString(R.string.key_import_db))
            prefImport.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val currentDB = activity.getDatabasePath(DatabaseHelper.DATABASE_NAME)
                DatabaseAdapter.getInstance().importDB(currentDB)
                ToastHelper.showToast(activity, getString(R.string.import_db), Toast.LENGTH_SHORT)
                true
            }
            val prefExport = findPreference(getString(R.string.key_export_db))
            prefExport.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val currentDB = activity.getDatabasePath(DatabaseHelper.DATABASE_NAME)
                DatabaseAdapter.getInstance().exportDb(currentDB)
                ToastHelper.showToast(activity, getString(R.string.export_db), Toast.LENGTH_SHORT)
                true
            }
            val prefAddRss = findPreference(getString(R.string.key_add_rss))
            prefAddRss.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                presenter.onDebugAddRssClicked(DatabaseAdapter.getInstance())
                true
            }
        }

        prefLicense.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            presenter.onLicenseClicked()
            true
        }
    }

    override fun setUpdateInterval(index: Int, summary: String) {
        prefUpdateInterval.setValueIndex(index)
        prefUpdateInterval.summary = summary
    }

    override fun setAutoUpdateInMainUi(isAutoUpdateInMainUi: Boolean) {
        prefAutoUpdateInMainUi.isChecked = isAutoUpdateInMainUi
    }

    override fun setArticleSort(isNewArticleTop: Boolean) {
        prefArticleSort.isChecked = isNewArticleTop
    }

    override fun setInternalBrowser(isEnabled: Boolean) {
        prefInternalBrowser.isChecked = isEnabled
    }

    override fun setAllReadBehavior(index: Int, summary: String) {
        prefAllReadBehavior.setValueIndex(index)
        prefAllReadBehavior.summary = summary
    }

    override fun setSwipeDirection(index: Int, summary: String) {
        prefSwipeDirection.setValueIndex(index)
        prefSwipeDirection.summary = summary
    }

    override fun setLaunchTab(index: Int, summary: String) {
        prefLaunchTab.setValueIndex(index)
        prefLaunchTab.summary = summary
    }

    override fun startLicenseActivity() {
        startActivity(Intent(activity, com.phicdy.mycuration.presentation.view.activity.LicenseActivity::class.java))
    }
}
