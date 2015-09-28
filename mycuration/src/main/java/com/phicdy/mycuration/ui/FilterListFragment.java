package com.phicdy.mycuration.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.filter.Filter;

import java.util.ArrayList;

public class FilterListFragment extends Fragment {

	private ArrayList<Filter> filters;
	private DatabaseAdapter dbAdapter;
	private FiltersListAdapter filtersListAdapter;
	private ListView filtersListView;

	private static final int DELETE_FILTER_MENU_ID = 2000;

	public FilterListFragment(){}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dbAdapter = DatabaseAdapter.getInstance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_filter_list, container, false);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		filtersListView = (ListView)getActivity().findViewById(R.id.lv_filter);
		TextView emptyView = (TextView)getActivity().findViewById(R.id.filter_emptyView);
		if (dbAdapter.getNumOfFeeds() == 0) {
			emptyView.setText(R.string.no_feed_message);
		}
		filtersListView.setEmptyView(emptyView);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	   
	    super.onCreateContextMenu(menu, v, menuInfo);

		menu.clear();
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
	public void onResume() {
		super.onResume();
		filters = dbAdapter.getAllFilters();

		initListView();
		registerForContextMenu(filtersListView);
	}

	private void initListView() {
		//Set ListView
		filtersListAdapter = new FiltersListAdapter(filters);
		filtersListView.setAdapter(filtersListAdapter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	/**
	 * 
	 * @author phicdy
	 * Display filters list
	 */
	class FiltersListAdapter extends ArrayAdapter<Filter> {
		public FiltersListAdapter(ArrayList<Filter> filters) {
			/*
			 * @param cotext
			 * @param int : Resource ID
			 * @param T[] objects : data list
			 */
			super(getActivity(),R.layout.filters_list,filters);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			
			//Use contentView
			View row = convertView;
			if(convertView == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
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
