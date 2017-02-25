package com.phicdy.mycuration.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.rss.Curation;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.phicdy.mycuration.view.activity.AddCurationActivity;

import java.util.ArrayList;

import static com.phicdy.mycuration.view.fragment.AddCurationFragment.EDIT_CURATION_ID;

public class CurationListFragment extends Fragment {

    private CurationListAdapter curationListAdapter;
    private OnCurationListFragmentListener mListener;
    private ListView curationListView;

    private ArrayList<Curation> allCurations = new ArrayList<>();
    private DatabaseAdapter dbAdapter;
    private UnreadCountManager unreadManager;

    private static final int EDIT_CURATION_MENU_ID = 1;
    private static final int DELETE_CURATION_MENU_ID = 2;

    private static final String LOG_TAG = "FilFeed.CurationList";

    public CurationListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unreadManager = UnreadCountManager.getInstance(getActivity());
        dbAdapter = DatabaseAdapter.getInstance(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.clear();
        menu.add(0, EDIT_CURATION_MENU_ID, 0, R.string.edit_curation);
        menu.add(0, DELETE_CURATION_MENU_ID, 0, R.string.delete_curation);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case EDIT_CURATION_MENU_ID:
                Intent intent = new Intent();
                intent.setClass(getActivity(), AddCurationActivity.class);
                intent.putExtra(EDIT_CURATION_ID, allCurations.get(info.position).getId());
                startActivity(intent);
                return true;
            case DELETE_CURATION_MENU_ID:
                dbAdapter.deleteCuration(allCurations.get(info.position).getId());
                allCurations.remove(info.position);
                curationListAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setAllListener() {
        // When an curation selected, display unread articles in the curation
        curationListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        mListener.onCurationListClicked(position);
                    }

                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_curation_list, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnCurationListFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnCurationListFragmentListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        curationListView = (ListView) getActivity().findViewById(R.id.lv_curation);
        TextView emptyView = (TextView)getActivity().findViewById(R.id.emptyView_curation);
        if (dbAdapter.getNumOfFeeds() == 0) {
            emptyView.setText(R.string.no_rss_message);
        }
        curationListView.setEmptyView(emptyView);
        getActivity().registerForContextMenu(curationListView);
        setAllListener();

        refreshList();

        // Set ListView
        curationListAdapter = new CurationListAdapter(allCurations, getActivity());
        curationListView.setAdapter(curationListAdapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void refreshList() {
        allCurations = dbAdapter.getAllCurations();
        curationListAdapter = new CurationListAdapter(allCurations, getActivity());
        curationListView.setAdapter(curationListAdapter);
        registerForContextMenu(curationListView);
        curationListAdapter.notifyDataSetChanged();
    }

    public int getCurationIdAtPosition (int position) {
        if (position < 0) {
            return -1;
        }

        if (allCurations == null || position > allCurations.size()-1) {
            return -1;
        }
        return allCurations.get(position).getId();
    }

    public interface OnCurationListFragmentListener {
        void onCurationListClicked(int position);
    }

    /**
     *
     */
    class CurationListAdapter extends ArrayAdapter<Curation> {
        CurationListAdapter(ArrayList<Curation> curations, Context context) {
            super(context, R.layout.curation_list, curations);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;

            // Use contentView and setup ViewHolder
            View row = convertView;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.curation_list, parent, false);
                holder = new ViewHolder();
                holder.curationName = (TextView) row.findViewById(R.id.tv_curation_title);
                holder.curationCount = (TextView) row.findViewById(R.id.tv_curation_count);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            Curation curation = this.getItem(position);
            if (curation != null) {
                holder.curationName.setText(curation.getName());
                holder.curationCount.setText(String.valueOf(unreadManager.getCurationCount(curation.getId())));
            }
            return row;
        }

        private class ViewHolder {
            TextView curationName;
            TextView curationCount;
        }
    }

}
