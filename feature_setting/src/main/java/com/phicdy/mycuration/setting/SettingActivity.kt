package com.phicdy.mycuration.setting


import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import com.phicdy.mycuration.feature.util.applySystemBarInsetsAsPadding
import com.phicdy.mycuration.feature.util.changeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingActivity : AppCompatActivity(), SettingFragment.OnSettingFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        setContentView(R.layout.activity_setting)
        findViewById<View>(R.id.app_bar_setting).applySystemBarInsetsAsPadding(top = true)
        findViewById<View>(R.id.fr_setting).applySystemBarInsetsAsPadding(bottom = true)

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
