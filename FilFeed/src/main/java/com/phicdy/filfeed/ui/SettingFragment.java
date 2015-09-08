package com.phicdy.filfeed.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.phicdy.filfeed.BuildConfig;
import com.phicdy.filfeed.R;
import com.phicdy.filfeed.alarm.AlarmManagerTaskManager;
import com.phicdy.filfeed.db.DatabaseAdapter;
import com.phicdy.filfeed.util.PreferenceHelper;
import com.phicdy.filfeed.util.ToastHelper;

public class SettingFragment extends PreferenceFragment {

    private ListPreference prefUpdateInterval;
    private ListPreference prefAllReadBehavior;
    private ListPreference prefSwipeDirection;
    private SwitchPreference prefArticleSort;
    private SwitchPreference prefInternalBrowser;

    private PreferenceHelper helper;
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
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        helper = PreferenceHelper.getInstance(getActivity());
        initView();
        initListener();
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
        // Set defalut value of update interval
        prefUpdateInterval = (ListPreference)findPreference(getString(R.string.key_update_interval));
        // Calc interval hour from saved interval second
        int autoUpdateIntervalSecond = helper.getAutoUpdateIntervalSecond();
        int autoUpdateIntervalHour = autoUpdateIntervalSecond / (60 * 60);
        String updateIntervalHourItems[] = getResources().getStringArray(R.array.update_interval_items_values);
        // Set index of saved interval
        for (int i = 0; i < updateIntervalHourItems.length; i++) {
            if (Integer.valueOf(updateIntervalHourItems[i]) == autoUpdateIntervalHour) {
                prefUpdateInterval.setValueIndex(i);
                // Set summary of saved interval
                String updateIntervalStringItems[] = getResources().getStringArray(R.array.update_interval_items);
                prefUpdateInterval.setSummary(updateIntervalStringItems[i]);
                break;
            }
        }

        // Set default value of article sort option
        prefArticleSort = (SwitchPreference)findPreference(getString(R.string.key_article_sort));
        prefArticleSort.setChecked(helper.getSortNewArticleTop());

        // Set default value of internal browser option
        prefInternalBrowser = (SwitchPreference)findPreference(getString(R.string.key_internal_browser));
        prefInternalBrowser.setChecked(helper.isOpenInternal());

        prefAllReadBehavior = (ListPreference)findPreference(getString(R.string.key_all_read_behavior));
        String allReadBehaviorItems[] = getResources().getStringArray(R.array.all_read_behavior_values);
        // Set index of behavior of all read
        for (int i = 0; i < allReadBehaviorItems.length; i++) {
            boolean allBehaviorItemBool = (Integer.valueOf(allReadBehaviorItems[i]) == 1);
            boolean savedValue = helper.getAllReadBack();
            if (allBehaviorItemBool == savedValue) {
                prefAllReadBehavior.setValueIndex(i);
                // Set summary of behavior of all read
                String allReadBehaviorStringItems[] = getResources().getStringArray(R.array.all_read_behavior);
                prefAllReadBehavior.setSummary(allReadBehaviorStringItems[i]);
                break;
            }
        }

        prefSwipeDirection = (ListPreference)findPreference(getString(R.string.key_swipe_direction));
        String swipeDirectionItems[] = getResources().getStringArray(R.array.swipe_direction_items_values);
        // Set index of swipe direction
        for (int i = 0; i < swipeDirectionItems.length; i++) {
            if (Integer.valueOf(swipeDirectionItems[i]) == helper.getSwipeDirection()) {
                prefSwipeDirection.setValueIndex(i);
                // Set summary of swipe direction
                String swipeDirectionStringItems[] = getResources().getStringArray(R.array.swipe_direction_items);
                prefSwipeDirection.setSummary(swipeDirectionStringItems[i]);
                break;
            }
        }
    }

    private void initListener() {
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(getString(R.string.key_update_interval))) {
                    updateUpdateInterval();
                } else if (key.equals(getString(R.string.key_all_read_behavior))) {
                    updateAllReadBehavior();
                } else if (key.equals(getString(R.string.key_swipe_direction))) {
                    updateSwipeDirection();
                } else if (key.equals(getString(R.string.key_article_sort))) {
                    updateArticleSort();
                } else if (key.equals(getString(R.string.key_internal_browser))) {
                    updateInternalBrowser();
                }
            }
        };
        if (BuildConfig.DEBUG) {
            Preference prefImport = findPreference(getString(R.string.key_import_db));
            prefImport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DatabaseAdapter.getInstance(getActivity()).importDB();
                    ToastHelper.showToast(getActivity(), getString(R.string.import_db), Toast.LENGTH_SHORT);
                    return true;
                }
            });
            Preference prefExport = findPreference(getString(R.string.key_export_db));
            prefExport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DatabaseAdapter.getInstance(getActivity()).exportDb();
                    ToastHelper.showToast(getActivity(), getString(R.string.export_db), Toast.LENGTH_SHORT);
                    return true;
                }
            });
        }
    }

    public void updateUpdateInterval() {
        // Save new interval second
        int intervalHour = Integer.valueOf(prefUpdateInterval.getValue());
        int intervalSecond = intervalHour * 60 * 60;
        helper.setAutoUpdateIntervalSecond(intervalSecond);
        String updateIntervalHourItems[] = getResources().getStringArray(R.array.update_interval_items_values);

        // Refresh summary
        for (int i = 0; i < updateIntervalHourItems.length; i++) {
            if (Integer.valueOf(updateIntervalHourItems[i]) == intervalHour) {
                String updateIntervalStringItems[] = getResources().getStringArray(R.array.update_interval_items);
                prefUpdateInterval.setSummary(updateIntervalStringItems[i]);
                break;
            }
        }

        // Set new alarm
        AlarmManagerTaskManager.setNewAlarm(getActivity());
    }

    public void updateAllReadBehavior() {
        // Save new behavior of all read
        boolean isAllReadBack = (Integer.valueOf(prefAllReadBehavior.getValue()) == 1);
        helper.setAllReadBack(isAllReadBack);
        String allReadBehaviorItems[] = getResources().getStringArray(R.array.all_read_behavior_values);

        // Refresh summary
        for (int i = 0; i < allReadBehaviorItems.length; i++) {
            boolean allBehaviorItemBool = (Integer.valueOf(allReadBehaviorItems[i]) == 1);
            if (allBehaviorItemBool == isAllReadBack) {
                String allReadBehaviorStringItems[] = getResources().getStringArray(R.array.all_read_behavior);
                prefAllReadBehavior.setSummary(allReadBehaviorStringItems[i]);
                break;
            }
        }
    }

    public void updateSwipeDirection() {
        // Save new swipe direction
        int swipeDirection = Integer.valueOf(prefSwipeDirection.getValue());
        helper.setSwipeDirection(swipeDirection);
        String swipeDirectionItems[] = getResources().getStringArray(R.array.swipe_direction_items_values);

        // Refresh summary
        for (int i = 0; i < swipeDirectionItems.length; i++) {
            if (Integer.valueOf(swipeDirectionItems[i]) == swipeDirection) {
                String swipeDirectionStringItems[] = getResources().getStringArray(R.array.swipe_direction_items);
                prefSwipeDirection.setSummary(swipeDirectionStringItems[i]);
                break;
            }
        }
    }

    public void updateArticleSort() {
        helper.setSortNewArticleTop(prefArticleSort.isChecked());
    }

    public void updateInternalBrowser() {
        helper.setOpenInternal(prefInternalBrowser.isChecked());
    }

}
