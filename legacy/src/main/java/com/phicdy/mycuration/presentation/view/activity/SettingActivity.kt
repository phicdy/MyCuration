package com.phicdy.mycuration.presentation.view.activity


import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.phicdy.mycuration.feature.util.changeTheme
import com.phicdy.mycuration.legacy.R
import com.phicdy.mycuration.presentation.view.fragment.SettingFragment

class SettingActivity : AppCompatActivity(), SettingFragment.OnSettingFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_setting)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.setting)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // For arrow button on toolbar
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        changeTheme()
    }

    override fun onThemeChanged(mode: Int) {
        delegate.setLocalNightMode(mode)
    }
}
