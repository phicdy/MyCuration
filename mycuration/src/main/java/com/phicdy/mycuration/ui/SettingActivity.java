package com.phicdy.mycuration.ui;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SettingActivity extends PreferenceActivity {

	private SettingFragment fragment = new SettingFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, fragment)
				.commit();
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
}
