package com.phicdy.mycuration.presentation.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.phicdy.mycuration.R
import com.phicdy.mycuration.presentation.presenter.InternalWebViewPresenter
import com.phicdy.mycuration.presentation.view.InternalWebViewView

class InternalWebViewActivity : AppCompatActivity(), InternalWebViewView {

    private var url: String? = null
    private lateinit var webView: WebView
    private lateinit var presenter: InternalWebViewPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internal_web_view)
        url = intent.getStringExtra(ArticlesListActivity.OPEN_URL_ID)
        presenter = InternalWebViewPresenter(this, url)
        presenter.create()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate menu resource file.
        menuInflater.inflate(R.menu.menu_internal_web_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_share -> presenter.onShareMenuClicked()
            // For arrow button on toolbar
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initWebView() {
        webView = findViewById(R.id.internal_web_view) as WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.builtInZoomControls = true
        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return false
            }
        })
    }

    override fun initToolbar() {
        val toolbar = findViewById(R.id.toolbar_internal_webview) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            // Show back arrow icon
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setTitle(R.string.app_name)
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
    }
}
