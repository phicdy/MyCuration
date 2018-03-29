package com.phicdy.mycuration.presentation.view.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

import com.phicdy.mycuration.R

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class InternalWebViewActivity : AppCompatActivity() {

    private var url: String? = null
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internal_web_view)

        // Set feed id and url from main activity
        url = intent.getStringExtra(ArticlesListActivity.OPEN_URL_ID)

        initWebView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate menu resource file.
        menuInflater.inflate(R.menu.menu_internal_web_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, url)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        if (url == null || !(url!!.startsWith("http://") || url!!.startsWith("https://"))) {
            return
        }
        webView = findViewById(R.id.internal_web_view) as WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.builtInZoomControls = true
        webView.loadUrl(url)
        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return false
            }
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
