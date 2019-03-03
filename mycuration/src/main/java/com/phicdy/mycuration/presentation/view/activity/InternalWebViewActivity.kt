package com.phicdy.mycuration.presentation.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import com.phicdy.mycuration.R
import com.phicdy.mycuration.presentation.presenter.InternalWebViewPresenter
import com.phicdy.mycuration.presentation.view.InternalWebViewView
import com.phicdy.mycuration.util.changeTheme

class InternalWebViewActivity : AppCompatActivity(), InternalWebViewView {

    private lateinit var mobileUserAgent: String
    private lateinit var webView: WebView
    private lateinit var scrollView: NestedScrollView
    private lateinit var presenter: InternalWebViewPresenter

    companion object {
        const val KEY_OPEN_URL = "openUrl"
        const val KEY_RSS_TITLE = "rssTitle"
        const val pcUserAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.63 Safari/537.36";
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internal_web_view)
        val url = intent.getStringExtra(KEY_OPEN_URL) ?: ""
        presenter = InternalWebViewPresenter(this, url)
        presenter.create()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            if (webView.settings.userAgentString == pcUserAgent) {
                menu.findItem(R.id.menu_pc_mode).isVisible = false
                menu.findItem(R.id.menu_mobile_mode).isVisible = true
            } else {
                menu.findItem(R.id.menu_pc_mode).isVisible = true
                menu.findItem(R.id.menu_mobile_mode).isVisible = false
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate menu resource file.
        menuInflater.inflate(R.menu.menu_internal_web_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_share -> presenter.onShareMenuClicked()
            R.id.menu_pc_mode -> presenter.onPcModeMenuClicked()
            R.id.menu_mobile_mode -> presenter.onMobileModeMenuClicked()
            // For arrow button on toolbar
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        changeTheme()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initWebView() {
        webView = findViewById(R.id.internal_web_view) as WebView
        mobileUserAgent = webView.settings.userAgentString
        webView.settings.javaScriptEnabled = true
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = true
        webView.settings.useWideViewPort = true
        webView.settings.setSupportZoom(true)
        webView.isFocusableInTouchMode = true
        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                scrollView.scrollY = 0
                return false
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                scrollView.scrollY = 0
                return false
            }

        })
        scrollView = findViewById(R.id.nsv_internal_web_view) as NestedScrollView
    }

    override fun initToolbar() {
        val toolbar = findViewById(R.id.toolbar_internal_webview) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            // Show back arrow icon
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            val rssTitle = intent.getStringExtra(KEY_RSS_TITLE)
            actionBar.title = rssTitle
        }
    }

    override fun load(url: String) {
        webView.loadUrl(url)
    }

    override fun share(url: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, url)
        startActivity(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return presenter.onKeyDown(keyCode, event, webView.canGoBack())
    }

    override fun parentOnKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    override fun goBack() {
        webView.goBack()
        scrollView.scrollY = 0
    }

    override fun setPcMode() {
        webView.settings.userAgentString = pcUserAgent
        invalidateOptionsMenu()
    }

    override fun setMobileMode() {
        webView.settings.userAgentString = mobileUserAgent
        invalidateOptionsMenu()
    }
}
