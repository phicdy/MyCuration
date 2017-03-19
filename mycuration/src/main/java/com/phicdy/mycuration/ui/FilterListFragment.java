package com.phicdy.mycuration.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.filter.Filter;
import com.phicdy.mycuration.presenter.FilterListPresenter;
import com.phicdy.mycuration.view.FilterListView;
import com.phicdy.mycuration.view.activity.RegisterFilterActivity;

import java.util.ArrayList;

public class FilterListFragment extends Fragment implements FilterListView {

	private FilterListPresenter presenter;
	private DatabaseAdapter dbAdapter;
	private FiltersListAdapter filtersListAdapter;
	private ListView filtersListView;

	private static final int EDIT_FILTER_MENU_ID = 2000;
	private static final int DELETE_FILTER_MENU_ID = 2001;

	public static final String KEY_EDIT_FILTER_ID = "editFilterId";

	public FilterListFragment(){}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        dbAdapter = DatabaseAdapter.getInstance(getActivity());
        presenter = new FilterListPresenter(dbAdapter);
        presenter.setView(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_filter_list, container, false);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		filtersListView = (ListView)getActivity().findViewById(R.id.lv_filter);
		TextView emptyView = (TextView)getActivity().findViewById(R.id.filter_emptyView);
		if (dbAdapter.getNumOfFeeds() == 0) {
			emptyView.setText(R.string.no_rss_message);
		}
		filtersListView.setEmptyView(emptyView);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    menu.add(0, EDIT_FILTER_MENU_ID, 0, R.string.edit_filter);
	    menu.add(0, DELETE_FILTER_MENU_ID, 1, R.string.delete_filter);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if (info.position > filtersListAdapter.getCount()-1) return false;
        Filter selectedFilter = filtersListAdapter.getItem(info.position);
        if (selectedFilter == null) return false;
	    switch (item.getItemId()) {
	    case DELETE_FILTER_MENU_ID:
	    	//Delte selected filter from DB and ListView
            presenter.onDeleteMenuClicked(info.position, selectedFilter);
	        return true;
		case EDIT_FILTER_MENU_ID:
		    presenter.onEditMenuClicked(selectedFilter);
			return true;
	    default:
	        return super.onContextItemSelected(item);
	    }
	}
	
	@Override
	public void onResume() {
		super.onResume();
        presenter.resume();
	}

	@Override
	public void initList(@NonNull ArrayList<Filter> filters) {
		//Set ListView
		filtersListAdapter = new FiltersListAdapter(filters);
		filtersListView.setAdapter(filtersListAdapter);
        registerForContextMenu(filtersListView);
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

    @Override
    public void remove(int position) {
        filtersListAdapter.remove(filtersListAdapter.getItem(position));
    }

    @Override
    public void notifyListChanged() {
        filtersListAdapter.notifyDataSetChanged();
    }

    @Override
    public void startEditActivity(int filterId) {
        Intent intent = new Intent(getActivity(), RegisterFilterActivity.class);
        intent.putExtra(KEY_EDIT_FILTER_ID, filterId);
        startActivity(intent);
    }

    /**
	 * 
	 * @author phicdy
	 * Display filters list
	 */
    private class FiltersListAdapter extends ArrayAdapter<Filter> {
		FiltersListAdapter(ArrayList<Filter> filters) {
			/*
			 * @param cotext
			 * @param int : Resource ID
			 * @param T[] objects : data list
			 */
			super(getActivity(),R.layout.filters_list,filters);
		}

		@NonNull
        @Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			ViewHolder holder;
			
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
				holder.filterEnabled = (Switch) row.findViewById(R.id.sw_filter_enable);
				row.setTag(holder);
			}else {
				holder = (ViewHolder)row.getTag();
			}
			
			final Filter filter = this.getItem(position);
			
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
				holder.filterUrl.setText(getString(R.string.url, url));

				final int p = position;
				holder.filterEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Filter clickedFilter = getItem(p);
                        if (clickedFilter != null) {
                            presenter.onFilterCheckClicked(clickedFilter, isChecked);
                        }
                    }
				});
				holder.filterEnabled.setChecked(filter.isEnabled());
			}
			return row;
		}
		
		private class ViewHolder {
			TextView filterTitle;
			TextView feedTitle;
			TextView filterKeyword;
			TextView filterUrl;
			Switch filterEnabled;
		}
	}
}
