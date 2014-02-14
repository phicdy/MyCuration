package com.example.rssfilterreader;

import java.util.ArrayList;

import android.R.id;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class FilterList extends ListActivity {

	private ArrayList<Filter> filters;
	private DatabaseHelper dbHelper = new DatabaseHelper(this);
	private DatabaseAdapter dbAdapter;
	private FiltersListAdapter filtersListAdapter;
	private ListView filtersListView;
	private Intent intent;
	
	private static final int DELETE_FILTER_MENU_ID = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filters_activity);
		dbAdapter = new DatabaseAdapter(this);
		displayFilters();
		registerForContextMenu(filtersListView);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	   
	    super.onCreateContextMenu(menu, v, menuInfo);
	   
	    menu.add(0, DELETE_FILTER_MENU_ID, 0, R.string.delete_filter);
	}
	  
	public boolean onContextItemSelected(MenuItem item) {
	   
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	   
	    switch (item.getItemId()) {
	    case DELETE_FILTER_MENU_ID:
	    	//Delte selected filter from DB and ListView
	        dbAdapter.deleteFilter(filters.get(info.position).getId());
	        filters.remove(info.position);
	        filtersListAdapter.notifyDataSetChanged();
	        return true;
	    default:
	        return super.onContextItemSelected(item);
	    }
	}
	
	private void displayFilters() {
		
		//If no feeds are added , back to main activity
		if(dbAdapter.getNumOfFeeds() == 0) {
			Toast.makeText(this, R.string.feed_not_exist, Toast.LENGTH_SHORT).show();
			startActivity(new Intent(FilterList.this, MainActivity.class));
		}
		
		//Get filters from DB 
		SQLiteDatabase rdb = dbHelper.getReadableDatabase();
		String sql         = "select _id,title,keyword,url,feedId from filters";
		Cursor cursor      = rdb.rawQuery(sql, null);
		filters = new ArrayList<Filter>();
		if(cursor != null) {
			while(cursor.moveToNext()) {
				int id         = cursor.getInt(0);
				String title   = cursor.getString(1);
				String keyword = cursor.getString(2);
				String url     = cursor.getString(3);
				int feedId     = cursor.getInt(4);
				Filter filter  = new Filter(id,title,keyword,url,feedId);
				filters.add(filter);
			}
		}
		
		//Set ListView
		filtersListView = (ListView)findViewById(id.list); 
		filtersListAdapter        = new FiltersListAdapter(filters);
		filtersListView.setAdapter(filtersListAdapter);
		
		TextView addTextView = (TextView)findViewById(R.id.addFilter);
		addTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				intent = new Intent(FilterList.this,RegisterFilter.class);
				startActivity(intent);
			}
		});
		
		//Close DB
		rdb.close();

	}


	
	@Override
	protected void onPause() {
		//If "+" button don't clicked, intent == null
		if(intent == null) {
			//Back to main
			intent = new Intent(FilterList.this,MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		super.onPause();
	}



	/**
	 * 
	 * @author kyamaguchi
	 * Display filters list
	 */
	class FiltersListAdapter extends ArrayAdapter<Filter> {
		public FiltersListAdapter(ArrayList<Filter> filters) {
			/*
			 * @param cotext
			 * @param int : Resource ID
			 * @param T[] objects : data list
			 */
			super(FilterList.this,R.layout.filters_list,filters);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			//Use contentView
			View row = convertView;
			if(convertView == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.filters_list, parent, false);
			}
			
			Filter filter = this.getItem(position);
			
			if(filter != null) {
				//set filter title
				TextView filterTitle = (TextView)row.findViewById(R.id.filterTitle);
				filterTitle.setText(filter.getTitle());
			}
			
			return(row);
		}
		
		
	}
}
