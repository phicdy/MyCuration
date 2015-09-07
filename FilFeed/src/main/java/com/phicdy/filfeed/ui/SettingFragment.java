package com.phicdy.filfeed.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.phicdy.filfeed.R;

public class SettingFragment extends PreferenceFragment {

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_fragment);
    }

}
