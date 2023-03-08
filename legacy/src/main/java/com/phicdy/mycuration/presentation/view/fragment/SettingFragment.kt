package com.phicdy.mycuration.presentation.view.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.AdditionalSettingApi
import com.phicdy.mycuration.domain.alarm.AlarmManagerTaskManager
import com.phicdy.mycuration.domain.setting.SettingInitialData
import com.phicdy.mycuration.legacy.BuildConfig
import com.phicdy.mycuration.legacy.R
import com.phicdy.mycuration.license.LicenseActivity
import com.phicdy.mycuration.presentation.presenter.SettingPresenter
import com.phicdy.mycuration.presentation.view.SettingView
import com.phicdy.mycuration.tracker.TrackerHelper
import com.phicdy.mycuration.util.ToastHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class SettingFragment : PreferenceFragmentCompat(), SettingView, CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    @Inject
    lateinit var presenter: SettingPresenter

    @Inject
    lateinit var settingInitialData: SettingInitialData

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    @Inject
    lateinit var additionalSettingApi: AdditionalSettingApi

    private lateinit var prefUpdateInterval: ListPreference
    private lateinit var prefLaunchTab: ListPreference
    private lateinit var prefAllReadBehavior: ListPreference
    private lateinit var prefSwipeDirection: ListPreference
    private lateinit var prefAutoUpdateInMainUi: SwitchPreference
    private lateinit var prefTheme: ListPreference
    private lateinit var prefArticleSort: SwitchPreference
    private lateinit var prefInternalBrowser: SwitchPreference
    private lateinit var prefLicense: Preference
    private lateinit var prefReview: Preference

    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null
    private lateinit var fragmentListener: OnSettingFragmentListener

    interface OnSettingFragmentListener {
        fun onThemeChanged(mode: Int)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (BuildConfig.DEBUG) {
            addPreferencesFromResource(R.xml.setting_fragment_debug)
        } else {
            addPreferencesFromResource(R.xml.setting_fragment)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            fragmentListener = context as OnSettingFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnSettingFragmentListener")
        }
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
        prefUpdateInterval = requirePreference(R.string.key_update_interval)
        prefAutoUpdateInMainUi = requirePreference(R.string.key_auto_update_in_main_ui)
        prefTheme = requirePreference(R.string.key_theme)
        prefArticleSort = requirePreference(R.string.key_article_sort)
        prefInternalBrowser = requirePreference(R.string.key_internal_browser)
        prefAllReadBehavior = requirePreference(R.string.key_all_read_behavior)
        prefSwipeDirection = requirePreference(R.string.key_swipe_direction)
        prefLaunchTab = requirePreference(R.string.key_launch_tab)
        prefLicense = requirePreference(R.string.key_license)
        prefReview = requirePreference(R.string.key_review)
    }

    private fun <T : Preference> requirePreference(key: Int): T = findPreference(getString(key))
            ?: throw IllegalArgumentException("key not found")

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
                getString(R.string.key_theme) -> {
                    val theme = Integer.valueOf(prefTheme.value)
                    presenter.updateTheme(theme)
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
            val prefImport = requirePreference<Preference>(R.string.key_import_db)
            activity?.let { activity ->
                prefImport.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    launch {
                        val currentDb = activity.getDatabasePath(DATABASE_NAME)
                        presenter.onImportDatabaseClicked(currentDb)
                        ToastHelper.showToast(activity, getString(R.string.import_db), Toast.LENGTH_SHORT)
                    }
                    true
                }
                val prefExport = requirePreference<Preference>(R.string.key_export_db)
                prefExport.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    launch {
                        val currentDb = activity.getDatabasePath(DATABASE_NAME)
                        presenter.onExportDatabaseClicked(currentDb)
                        ToastHelper.showToast(activity, getString(R.string.export_db), Toast.LENGTH_SHORT)
                    }
                    true
                }
                val prefAddRss = requirePreference<Preference>(R.string.key_add_rss)
                prefAddRss.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    launch {
                        presenter.onDebugAddRssClicked()
                    }
                    true
                }
                val prefFixUnreadCount = requirePreference<Preference>(R.string.key_fix_unread_count)
                prefFixUnreadCount.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    launch {
                        presenter.onFixUnreadCount()
                    }
                    true
                }
            }
        }

        prefLicense.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            presenter.onLicenseClicked()
            true
        }

        prefReview.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            TrackerHelper.sendButtonEvent(getString(R.string.tap_go_to_google_play_from_setting))
            activity?.let { a ->
                try {
                    val uri = Uri.parse("market://details?id=${a.packageName}")
                    a.startActivity(Intent(Intent.ACTION_VIEW, uri))
                } catch (e: Exception) {
                }
            }
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

    override fun setTheme(index: Int, theme: String, mode: Int) {
        prefTheme.setValueIndex(index)
        prefTheme.summary = theme
        fragmentListener.onThemeChanged(mode)
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

    @Module
    @InstallIn(FragmentComponent::class)
    object SettingModule {
        @FragmentScoped
        @Provides
        fun provideSettingInitialData(@ApplicationContext context: Context): SettingInitialData {
            val updateIntervalHourItems = context.resources.getStringArray(R.array.update_interval_items_values)
            val updateIntervalStringItems = context.resources.getStringArray(R.array.update_interval_items)
            val themeItems = context.resources.getStringArray(R.array.theme_items_values)
            val themeStringItems = context.resources.getStringArray(R.array.theme_items)
            val allReadBehaviorItems = context.resources.getStringArray(R.array.all_read_behavior_values)
            val allReadBehaviorStringItems = context.resources.getStringArray(R.array.all_read_behavior)
            val launchTabItems = context.resources.getStringArray(R.array.launch_tab_items_values)
            val launchTabStringItems = context.resources.getStringArray(R.array.launch_tab_items)
            val swipeDirectionItems = context.resources.getStringArray(R.array.swipe_direction_items_values)
            val swipeDirectionStringItems = context.resources.getStringArray(R.array.swipe_direction_items)
            return SettingInitialData(
                    updateIntervalHourItems = updateIntervalHourItems,
                    updateIntervalStringItems = updateIntervalStringItems,
                    themeItems = themeItems,
                    themeStringItems = themeStringItems,
                    allReadBehaviorStringItems = allReadBehaviorStringItems,
                    allReadBehaviorItems = allReadBehaviorItems,
                    launchTabItems = launchTabItems,
                    launchTabStringItems = launchTabStringItems,
                    swipeDirectionItems = swipeDirectionItems,
                    swipeDirectionStringItems = swipeDirectionStringItems
            )
        }

        @FragmentScoped
        @Provides
        fun provideSettingView(fragment: Fragment): SettingView = fragment as SettingView
    }

    companion object {
        private const val DATABASE_NAME = "rss_manage"
    }
}
