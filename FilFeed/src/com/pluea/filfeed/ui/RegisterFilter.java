package com.pluea.filfeed.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.pluea.filfeed.R;
import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.db.DatabaseHelper;
import com.pluea.filfeed.rss.Feed;

public class RegisterFilter extends Activity {

	private DatabaseHelper dbHelper = new DatabaseHelper(this);
	private DatabaseAdapter dbAdapter;
	private String[] feedTitles;
	private ArrayList<Feed> feedsList;
	private int selectedFeedId; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_filter);
		
		setTitle(R.string.add_filter);
		
		dbAdapter = DatabaseAdapter.getInstance(this);
		
		//Select All feeds
		SQLiteDatabase rdb = dbHelper.getReadableDatabase();
		String getAllFeedsSql = "select _id,title,url from feeds";
		Cursor cur = rdb.rawQuery(getAllFeedsSql, null);
		if(cur.getCount() == 0) {
			//back
		}else {
			//Get all feeds
			feedTitles = new String[cur.getCount()];
			feedsList  = new ArrayList<Feed>();
			int indent = 0;
			while(cur.moveToNext()) {
				feedTitles[indent] = cur.getString(1);
				int id = cur.getInt(0);
				String title = cur.getString(1);
				String url = cur.getString(2);
				Feed feed = new Feed(id,title,url,"", "", 0);
				feedsList.add(feed);
				indent++;
			}
		
			//Set spinner
			Spinner targetFeedSpin = (Spinner)findViewById(R.id.targetFeed);
			ArrayAdapter<String> aa = new ArrayAdapter<String>(RegisterFilter.this,android.R.layout.simple_spinner_item,feedTitles);
			aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			
			targetFeedSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					//Update selected feed ID
					selectedFeedId = feedsList.get(position).getId();
				}
	
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
				
			});
			//Set default selected Feed ID
			selectedFeedId = feedsList.get(0).getId();
			
			targetFeedSpin.setAdapter(aa);
		
			rdb.close();
		}
		
		//If register button clicked, insert filter into DB
		Button registerButton = (Button)findViewById(R.id.registerFilter);
		registerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditText keyword     = (EditText)findViewById(R.id.filterKeyword);
				EditText filterUrl   = (EditText)findViewById(R.id.filterUrl);
				EditText title       = (EditText)findViewById(R.id.filterTitle);
				String keywordText   = keyword.getText().toString();
				String filterUrlText = filterUrl.getText().toString();
				String titleText     = title.getText().toString();
				
				//Check title and keyword or filter URL has the text
				if(titleText.equals("")) {
					Toast.makeText(RegisterFilter.this, R.string.title_empty_error, Toast.LENGTH_SHORT).show();
				}else if((keywordText.equals("")) && (filterUrlText.equals(""))) {
					Toast.makeText(RegisterFilter.this, R.string.both_keyword_and_url_empty_error, Toast.LENGTH_SHORT).show();
				}else if(keywordText.equals("%") || filterUrlText.equals("%")) {
					Toast.makeText(RegisterFilter.this, R.string.percent_only_error, Toast.LENGTH_SHORT).show();
				}else {
					dbAdapter.saveNewFilter(titleText, selectedFeedId, keywordText, filterUrlText);
					startActivity(new Intent(RegisterFilter.this,FilterList.class));
				}
			}
		});
		
		//If cancel button clicked, back to filters list 
		Button cancelButton = (Button)findViewById(R.id.cancelRegisterFilter);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(RegisterFilter.this,FilterList.class));
			}
		});
	}
	

}
