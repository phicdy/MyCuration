package com.pluea.filfeed.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import com.pluea.filfeed.R;

public class InternalWebViewActivity extends Activity {
	
	String url;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_internal_web_view);

		// Set feed id and url from main activity
		Intent intent = getIntent();
		url = intent.getStringExtra(ArticlesListActivity.OPEN_URL_ID);
		
		initWebView();
	}
	
	private void initWebView() {
		WebView webView = (WebView)findViewById(R.id.internal_web_view);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.loadUrl(url);
	}
}
