package com.phicdy.mycuration.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ShareActionProvider;

import com.phicdy.mycuration.R;

public class InternalWebViewActivity extends Activity {
	
	private String url;
	private ShareActionProvider mShareActionProvider;
	
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

	    // Locate MenuItem with ShareActionProvider
	    MenuItem item = menu.findItem(R.id.menu_item_share);

	    // Fetch and store ShareActionProvider
	    mShareActionProvider = (ShareActionProvider) item.getActionProvider();
	    
	    if(mShareActionProvider != null && url != null) {
	    	Intent intent = new Intent(Intent.ACTION_SEND);
	    	intent.setType("text/plain");
	    	intent.putExtra(Intent.EXTRA_TEXT, url);
	    	mShareActionProvider.setShareIntent(intent);
	    }

	    // Return true to display menu
	    return true;
	}
	
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
