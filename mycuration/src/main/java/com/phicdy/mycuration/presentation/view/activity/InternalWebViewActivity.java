package com.phicdy.mycuration.presentation.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.phicdy.mycuration.R;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class InternalWebViewActivity extends AppCompatActivity {
	
	private String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_internal_web_view);

		// Set feed id and url from main activity
		Intent intent = getIntent();
		url = intent.getStringExtra(ArticlesListActivity.OPEN_URL_ID);
		
		initWebView();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate menu resource file.
	    getMenuInflater().inflate(R.menu.menu_internal_web_view, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
			case R.id.menu_item_share:
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, url);
                startActivity(intent);
                break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
	
	@SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
		if(url == null || !(url.startsWith("http://") || url.startsWith("https://"))) {
			return;
		}
		WebView webView = (WebView)findViewById(R.id.internal_web_view);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.loadUrl(url);
	}
}
