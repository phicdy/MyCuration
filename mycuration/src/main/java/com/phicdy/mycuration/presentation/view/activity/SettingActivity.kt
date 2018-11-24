package com.phicdy.mycuration.presentation.view.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.phicdy.mycuration.R


import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_setting)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.setting)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // For arrow button on toolbar
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
