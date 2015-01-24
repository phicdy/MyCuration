package com.pluea.filfeed.ui;

import java.util.ArrayList;

import android.R.id;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pluea.filfeed.R;
import com.pluea.filfeed.db.DatabaseAdapter;
import com.pluea.filfeed.filter.Filter;

public class FilterListActivity extends ActionBarActivity {

	private ArrayList<Filter> filters;
	private DatabaseAdapter dbAdapter;
	private FiltersListAdapter filtersListAdapter;
	private ListView filtersListView;
	
	private static final int DELETE_FILTER_MENU_ID = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter_list);
		setTitle(R.string.filter);
		
		dbAdapter = DatabaseAdapter.getInstance(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_filter_list, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.add_filter:
			startActivity(new Intent(FilterListActivity.this,RegisterFilterActivity.class));
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
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
	
	@Override
	protected void onResume() {
		super.onResume();
		filters = dbAdapter.getAllFilters();
		
		//If no feeds are added, back to main activity
		if(dbAdapter.getNumOfFeeds() == 0) {
			Toast.makeText(this, R.string.feed_not_exist, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		initListView();
		registerForContextMenu(filtersListView);
	}

	private void initListView() {
		//Set ListView
		filtersListView = (ListView)findViewById(id.list); 
		filtersListAdapter = new FiltersListAdapter(filters);
		filtersListView.setAdapter(filtersListAdapter);
	}
	
	@Override
	protected void onPause() {
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
			super(FilterListActivity.this,R.layout.filters_list,filters);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			
			//Use contentView
			View row = convertView;
			if(convertView == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.filters_list, parent, false);
				holder = new ViewHolder();
				holder.filterTitle = (TextView)row.findViewById(R.id.filterTitle);
				holder.feedTitle = (TextView) row.findViewById(R.id.filterTargetFeed);
				holder.filterKeyword = (TextView) row.findViewById(R.id.filterKeyword);
				holder.filterUrl = (TextView) row.findViewById(R.id.filterUrl);
				row.setTag(holder);
			}else {
				holder = (ViewHolder)row.getTag();
			}
			
			Filter filter = this.getItem(position);
			
			if(filter != null) {
				//set filter title
				holder.filterTitle.setText(filter.getTitle());
				
				holder.feedTitle.setText(filter.getFeedTitle());
				
				String keyword = filter.getKeyword();
				if (keyword == null || keyword.equals("")) {
					keyword = getString(R.string.none);
				}
				holder.filterKeyword.setText(getString(R.string.keyword) + ": " + keyword);
				
				String url = filter.getUrl();
				if (url == null || url.equals("")) {
					url = getString(R.string.none);
				}
				holder.filterUrl.setText("URL: " + url);
			}
			
			return row;
		}
		
		private class ViewHolder {
			TextView filterTitle;
			TextView feedTitle;
			TextView filterKeyword;
			TextView filterUrl;
		}
	}
}
