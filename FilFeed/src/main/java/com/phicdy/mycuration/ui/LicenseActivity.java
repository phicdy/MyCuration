package com.phicdy.mycuration.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.phicdy.mycuration.R;

public class LicenseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        setTitle(getString(R.string.license));
        WebView webView = (WebView)findViewById(R.id.license_web_view);
        webView.loadUrl("file:///android_asset/license.html");
    }

}
