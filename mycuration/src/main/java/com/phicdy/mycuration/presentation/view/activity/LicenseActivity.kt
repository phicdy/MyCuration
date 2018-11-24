package com.phicdy.mycuration.presentation.view.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import com.phicdy.mycuration.R
import io.github.inflationx.viewpump.ViewPumpContextWrapper

class LicenseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_license)

        title = getString(R.string.license)
        val webView = findViewById(R.id.license_web_view) as WebView
        webView.loadUrl("file:///android_asset/license.html")
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

}
