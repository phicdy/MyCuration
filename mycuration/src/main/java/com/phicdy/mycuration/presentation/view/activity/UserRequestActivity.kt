package com.phicdy.mycuration.presentation.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.phicdy.mycuration.R

class UserRequestActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_request)
        initToolbar()
        val webview = findViewById<WebView>(R.id.webview_user_review)
        webview.settings.javaScriptEnabled = true
        webview.loadUrl("https://docs.google.com/forms/d/e/1FAIpQLSdDR-vqD-yk_QzXdbVwiTwTHnl05p4ItHtJ_iOjGr-X0DOBIQ/viewform")
    }

    fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_user_review)
        toolbar.title = getString(R.string.title_activity_user_review)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            // Show back arrow icon
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> finish()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
