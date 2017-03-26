package com.phicdy.mycuration.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.view.fragment.ArticleSearchResultFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ArticleSearchResultActivity extends AppCompatActivity {

    private ArticleSearchResultFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_search_result);

		// Set feed id and url from main activity
        Intent intent = getIntent();
        fragment = (ArticleSearchResultFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fr_article_search_result);
        fragment.handleIntent(intent);

		setTitle(getString(R.string.search_result));
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		GATrackerHelper.sendScreen(getString(R.string.search_result));
	}

	@Override
	protected void onNewIntent(Intent intent) {
		fragment.handleIntent(intent);
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return true;
	}
}
