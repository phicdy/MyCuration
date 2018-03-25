package com.phicdy.mycuration.view.activity

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceActivity

import com.phicdy.mycuration.presentation.view.fragment.SettingFragment

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class SettingActivity : PreferenceActivity() {

    private val fragment = SettingFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
}
