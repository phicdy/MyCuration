package com.phicdy.mycuration.presentation.view.activity

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceActivity
import android.support.v7.app.AppCompatActivity

import com.phicdy.mycuration.presentation.view.fragment.SettingFragment

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class SettingActivity : AppCompatActivity() {

    private val fragment = SettingFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
}
