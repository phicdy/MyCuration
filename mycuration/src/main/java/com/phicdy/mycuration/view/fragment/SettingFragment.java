package com.phicdy.mycuration.view.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.phicdy.mycuration.BuildConfig;
import com.phicdy.mycuration.R;
import com.phicdy.mycuration.alarm.AlarmManagerTaskManager;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.presenter.SettingPresenter;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.util.PreferenceHelper;
import com.phicdy.mycuration.util.ToastHelper;
import com.phicdy.mycuration.view.SettingView;
import com.phicdy.mycuration.view.activity.LicenseActivity;

public class SettingFragment extends PreferenceFragment implements SettingView {
    private SettingPresenter presenter;

    private ListPreference prefUpdateInterval;
    private ListPreference prefAllReadBehavior;
    private ListPreference prefSwipeDirection;
    private SwitchPreference prefAutoUpdateInMainUi;
    private SwitchPreference prefArticleSort;
    private SwitchPreference prefInternalBrowser;
    private Preference prefLicense;

    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            addPreferencesFromResource(R.xml.setting_fragment_debug);
        } else {
            addPreferencesFromResource(R.xml.setting_fragment);
        }
        PreferenceHelper helper = PreferenceHelper.INSTANCE;
        String updateIntervalHourItems[] = getResources().getStringArray(R.array.update_interval_items_values);
        String updateIntervalStringItems[] = getResources().getStringArray(R.array.update_interval_items);
        String allReadBehaviorItems[] = getResources().getStringArray(R.array.all_read_behavior_values);
        String allReadBehaviorStringItems[] = getResources().getStringArray(R.array.all_read_behavior);
        String swipeDirectionItems[] = getResources().getStringArray(R.array.swipe_direction_items_values);
        String swipeDirectionStringItems[] = getResources().getStringArray(R.array.swipe_direction_items);
        presenter = new SettingPresenter(helper, updateIntervalHourItems,
                updateIntervalStringItems, allReadBehaviorItems, allReadBehaviorStringItems,
                swipeDirectionItems, swipeDirectionStringItems);
        presenter.setView(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initListener();
        presenter.activityCreate();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

    private void initView() {
        prefUpdateInterval = (ListPreference) findPreference(getString(R.string.key_update_interval));
        prefAutoUpdateInMainUi = (SwitchPreference)findPreference(getString(R.string.key_auto_update_in_main_ui));
        prefArticleSort = (SwitchPreference)findPreference(getString(R.string.key_article_sort));
        prefInternalBrowser = (SwitchPreference)findPreference(getString(R.string.key_internal_browser));
        prefAllReadBehavior = (ListPreference)findPreference(getString(R.string.key_all_read_behavior));
        prefSwipeDirection = (ListPreference)findPreference(getString(R.string.key_swipe_direction));
        prefLicense = findPreference(getString(R.string.key_license));
    }

    private void initListener() {
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(getString(R.string.key_update_interval))) {
                    int intervalHour = Integer.valueOf(prefUpdateInterval.getValue());
                    AlarmManagerTaskManager manager = new AlarmManagerTaskManager(getActivity());
                    presenter.updateUpdateInterval(intervalHour, manager);
                    // GA
                    GATrackerHelper.INSTANCE.sendEvent(getString(R.string.change_auto_update_interval), intervalHour);
                } else if (key.equals(getString(R.string.key_all_read_behavior))) {
                    boolean isAllReadBack = (Integer.valueOf(prefAllReadBehavior.getValue()) == 1);
                    presenter.updateAllReadBehavior(isAllReadBack);
                    // GA
                    GATrackerHelper.INSTANCE.sendEvent(getString(R.string.change_all_read_behavior), isAllReadBack ? 1 : 0);
                } else if (key.equals(getString(R.string.key_swipe_direction))) {
                    int swipeDirection = Integer.valueOf(prefSwipeDirection.getValue());
                    presenter.updateSwipeDirection(swipeDirection);
                    // GA
                    GATrackerHelper.INSTANCE.sendEvent(getString(R.string.change_swipe_direction), swipeDirection);
                } else if (key.equals(getString(R.string.key_article_sort))) {
                    boolean isNewArticleTop = prefArticleSort.isChecked();
                    presenter.updateArticleSort(isNewArticleTop);
                    // GA
                    GATrackerHelper.INSTANCE.sendEvent(getString(R.string.change_aricle_sort), prefArticleSort.isChecked() ? 1 : 0);
                } else if (key.equals(getString(R.string.key_internal_browser))) {
                    boolean isInternal = prefInternalBrowser.isChecked();
                    presenter.updateInternalBrowser(isInternal);
                    // GA
                    GATrackerHelper.INSTANCE.sendEvent(getString(R.string.change_browser_option), prefInternalBrowser.isChecked() ? 1 : 0);
                } else if (key.equals(getString(R.string.key_auto_update_in_main_ui))) {
                    boolean isAutoUpdateInMainUi = prefAutoUpdateInMainUi.isChecked();
                    presenter.updateAutoUpdateInMainUi(isAutoUpdateInMainUi);
                    GATrackerHelper.INSTANCE.sendEvent(getString(R.string.change_auto_update_in_main_ui_option), isAutoUpdateInMainUi ? 1: 0);
                }
            }
        };

        if (BuildConfig.DEBUG) {
            Preference prefImport = findPreference(getString(R.string.key_import_db));
            prefImport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DatabaseAdapter.getInstance(getActivity()).importDB();
                    ToastHelper.INSTANCE.showToast(getActivity(), getString(R.string.import_db), Toast.LENGTH_SHORT);
                    return true;
                }
            });
            Preference prefExport = findPreference(getString(R.string.key_export_db));
            prefExport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DatabaseAdapter.getInstance(getActivity()).exportDb();
                    ToastHelper.INSTANCE.showToast(getActivity(), getString(R.string.export_db), Toast.LENGTH_SHORT);
                    return true;
                }
            });
            Preference prefAddRss = findPreference(getString(R.string.key_add_rss));
            prefAddRss.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    presenter.onDebugAddRssClicked(DatabaseAdapter.getInstance(getActivity()));
                    return true;
                }
            });
        }

        prefLicense.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                presenter.onLicenseClicked();
                return true;
            }
        });
    }

    @Override
    public void setUpdateInterval(int index, @NonNull String summary) {
        prefUpdateInterval.setValueIndex(index);
        prefUpdateInterval.setSummary(summary);
    }

    @Override
    public void setAutoUpdateInMainUi(boolean isAutoUpdateInMainUi) {
        prefAutoUpdateInMainUi.setChecked(isAutoUpdateInMainUi);
    }

    @Override
    public void setArticleSort(boolean isNewArticleTop) {
        prefArticleSort.setChecked(isNewArticleTop);
    }

    @Override
    public void setInternalBrowser(boolean isEnabled) {
        prefInternalBrowser.setChecked(isEnabled);
    }

    @Override
    public void setAllReadBehavior(int index, @NonNull String summary) {
        prefAllReadBehavior.setValueIndex(index);
        prefAllReadBehavior.setSummary(summary);
    }

    @Override
    public void setSwipeDirection(int index, @NonNull String summary) {
        prefSwipeDirection.setValueIndex(index);
        prefSwipeDirection.setSummary(summary);
    }

    @Override
    public void startLicenseActivity() {
        startActivity(new Intent(getActivity(), LicenseActivity.class));
    }
}
