package com.phicdy.mycuration.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.tracker.GATrackerHelper;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RegisterFilterActivity extends ActionBarActivity {

	private DatabaseAdapter dbAdapter;
	private String[] feedTitles;
	private ArrayList<Feed> feedsList;
	private int selectedFeedId; 
	
	private Spinner targetFeedSpin;

	private GATrackerHelper gaTrackerHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_filter);
		
		dbAdapter = DatabaseAdapter.getInstance(this);
		initView();
		initData();

		gaTrackerHelper = GATrackerHelper.getInstance(this);
		gaTrackerHelper.sendScreen(getTitle().toString());
	}
	
	private void initView() {
		setTitle(R.string.add_filter);
		
		//Set spinner
		targetFeedSpin = (Spinner)findViewById(R.id.targetFeed);
		targetFeedSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				//Update selected feed ID
				selectedFeedId = feedsList.get(position).getId();
			}
	
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
			
		});
		
		//If register button clicked, insert filter into DB
		Button registerButton = (Button)findViewById(R.id.registerFilter);
		registerButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText keyword = (EditText) findViewById(R.id.filterKeyword);
				EditText filterUrl = (EditText) findViewById(R.id.filterUrl);
				EditText title = (EditText) findViewById(R.id.filterTitle);
				String keywordText = keyword.getText().toString();
				String filterUrlText = filterUrl.getText().toString();
				String titleText = title.getText().toString();

				//Check title and keyword or filter URL has the text
				if (titleText.equals("")) {
					Toast.makeText(RegisterFilterActivity.this, R.string.title_empty_error, Toast.LENGTH_SHORT).show();
					gaTrackerHelper.sendEvent(getString(R.string.add_new_filter_no_title));
				} else if ((keywordText.equals("")) && (filterUrlText.equals(""))) {
					Toast.makeText(RegisterFilterActivity.this, R.string.both_keyword_and_url_empty_error, Toast.LENGTH_SHORT).show();
					gaTrackerHelper.sendEvent(getString(R.string.add_new_filter_no_keyword_url));
				} else if (keywordText.equals("%") || filterUrlText.equals("%")) {
					Toast.makeText(RegisterFilterActivity.this, R.string.percent_only_error, Toast.LENGTH_SHORT).show();
				} else {
					gaTrackerHelper.sendEvent(getString(R.string.add_new_filter));
					dbAdapter.saveNewFilter(titleText, selectedFeedId, keywordText, filterUrlText);
					finish();
				}
			}
		});
		
		//If cancel button clicked, back to filters list 
		Button cancelButton = (Button)findViewById(R.id.cancelRegisterFilter);
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
				gaTrackerHelper.sendEvent(getString(R.string.add_new_filter_cancel));
			}
		});
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
	
	private void initData() {
		feedsList = dbAdapter.getAllFeedsWithoutNumOfUnreadArticles();
		if(feedsList.size() == 0) {
			finish();
		}else {
			// Init feed title list for spinner
			feedTitles = new String[feedsList.size()];
			for (int i = 0; i < feedsList.size(); i++) {
				feedTitles[i] = feedsList.get(i).getTitle();
			}
		}
		//Set default selected Feed ID
		selectedFeedId = feedsList.get(0).getId();
		
		// Set data for spinner
		ArrayAdapter<String> aa = new ArrayAdapter<String>(RegisterFilterActivity.this,android.R.layout.simple_spinner_item,feedTitles);
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		targetFeedSpin.setAdapter(aa);
	}
}
