package com.phicdy.mycuration.presentation.view.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.widget.Toast
import com.phicdy.mycuration.BuildConfig
import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.alarm.AlarmManagerTaskManager
import com.phicdy.mycuration.presentation.presenter.SettingPresenter
import com.phicdy.mycuration.presentation.view.SettingView
import com.phicdy.mycuration.presentation.view.activity.LicenseActivity
import com.phicdy.mycuration.tracker.TrackerHelper
import com.phicdy.mycuration.util.PreferenceHelper
import com.phicdy.mycuration.util.ToastHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext


class SettingFragment : PreferenceFragmentCompat(), SettingView, CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private lateinit var presenter: SettingPresenter

    private lateinit var prefUpdateInterval: ListPreference
    private lateinit var prefLaunchTab: ListPreference
    private lateinit var prefAllReadBehavior: ListPreference
    private lateinit var prefSwipeDirection: ListPreference
    private lateinit var prefAutoUpdateInMainUi: SwitchPreference
    private lateinit var prefArticleSort: SwitchPreference
    private lateinit var prefInternalBrowser: SwitchPreference
    private lateinit var prefLicense: Preference

    private val rssRepository: RssRepository by inject()
    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
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
        presenter = SettingPresenter(helper, rssRepository, updateIntervalHourItems,
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
                    val manager = AlarmManagerTaskManager(activity as Context)
                    presenter.updateUpdateInterval(intervalHour, manager)
                    // GA
                    TrackerHelper.sendSettingEvent(getString(R.string.change_auto_update_interval), intervalHour.toLong().toString())
                }
                getString(R.string.key_all_read_behavior) -> {
                    val isAllReadBack = Integer.valueOf(prefAllReadBehavior.value) == 1
                    presenter.updateAllReadBehavior(isAllReadBack)
                    // GA
                    val allReadBehaviorStringItems = resources.getStringArray(R.array.all_read_behavior)
                    TrackerHelper.sendSettingEvent(getString(R.string.change_all_read_behavior),
                            allReadBehaviorStringItems[Integer.valueOf(prefAllReadBehavior.value)])
                }
                getString(R.string.key_swipe_direction) -> {
                    val swipeDirection = Integer.valueOf(prefSwipeDirection.value)
                    presenter.updateSwipeDirection(swipeDirection)
                    // GA
                    val swipeDirectionStringItems = resources.getStringArray(R.array.swipe_direction_items)
                    TrackerHelper.sendSettingEvent(getString(R.string.change_swipe_direction),
                            swipeDirectionStringItems[Integer.valueOf(prefSwipeDirection.value)])
                }
                getString(R.string.key_article_sort) -> {
                    val isNewArticleTop = prefArticleSort.isChecked
                    presenter.updateArticleSort(isNewArticleTop)
                    // GA
                    TrackerHelper.sendSettingEvent(getString(R.string.change_aricle_sort),
                            if (prefArticleSort.isChecked) getString(R.string.article_sort) else getString(R.string.not_article_sort))
                }
                getString(R.string.key_internal_browser) -> {
                    val isInternal = prefInternalBrowser.isChecked
                    presenter.updateInternalBrowser(isInternal)
                    // GA
                    TrackerHelper.sendSettingEvent(getString(R.string.change_browser_option),
                            if (prefInternalBrowser.isChecked) getString(R.string.open_internal) else getString(R.string.not_open_internal))
                }
                getString(R.string.key_auto_update_in_main_ui) -> {
                    val isAutoUpdateInMainUi = prefAutoUpdateInMainUi.isChecked
                    presenter.updateAutoUpdateInMainUi(isAutoUpdateInMainUi)
                    TrackerHelper.sendSettingEvent(getString(R.string.change_auto_update_in_main_ui_option),
                            if (isAutoUpdateInMainUi) getString(R.string.auto_update_in_main_ui) else getString(R.string.not_auto_update_in_main_ui))
                }
                getString(R.string.key_launch_tab) -> {
                    val launchTab = Integer.valueOf(prefLaunchTab.value)
                    presenter.updateLaunchTab(launchTab)
                }
            }
        }

        if (BuildConfig.DEBUG) {
            val prefImport = findPreference(getString(R.string.key_import_db))
            activity?.let { activity ->
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
                    launch { presenter.onDebugAddRssClicked() }
                    true
                }
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
        startActivity(Intent(activity, LicenseActivity::class.java))
    }
}
