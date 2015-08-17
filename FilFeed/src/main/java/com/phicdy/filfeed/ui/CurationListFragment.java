package com.phicdy.filfeed.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.phicdy.filfeed.R;
import com.phicdy.filfeed.db.DatabaseAdapter;
import com.phicdy.filfeed.rss.Curation;
import com.phicdy.filfeed.rss.UnreadCountManager;

import java.util.ArrayList;

public class CurationListFragment extends Fragment {

    private CurationListAdapter curationListAdapter;
    private OnCurationListFragmentListener mListener;
    private ListView curationListView;

    private ArrayList<Curation> allCurations = new ArrayList<>();
    private DatabaseAdapter dbAdapter;
    private UnreadCountManager unreadManager;

    private static final String LOG_TAG = "FilFeed.CurationList";

    public static CurationListFragment newInstance() {
        return new CurationListFragment();
    }

    public CurationListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        unreadManager = UnreadCountManager.getInstance(getActivity());
        dbAdapter = DatabaseAdapter.getInstance(getActivity());
        allCurations = dbAdapter.getAllCurations();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnCurationListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        curationListView = (ListView) getActivity().findViewById(R.id.lv_curation);
        TextView emptyView = (TextView)getActivity().findViewById(R.id.emptyView_curation);
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
        curationListAdapter = new CurationListAdapter(allCurations, getActivity());
        curationListView.setAdapter(curationListAdapter);
        curationListAdapter.notifyDataSetChanged();
    }

    public void addCuration(Curation newCuration) {
        allCurations.add(newCuration);
        curationListAdapter.notifyDataSetChanged();
    }

    public void removeCurationAtPosition(int position) {
        Curation deletedCuration = allCurations.get(position);
        dbAdapter.deleteCuration(deletedCuration.getId());
        allCurations.remove(position);
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
        public void onCurationListClicked(int position);
    }

    /**
     *
     * @author kyamaguchi Display RSS Curations List
     */
    class CurationListAdapter extends ArrayAdapter<Curation> {
        public CurationListAdapter(ArrayList<Curation> curations, Context context) {
            super(context, R.layout.curation_list, curations);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            // Use contentView and setup ViewHolder
            View row = convertView;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.curation_list, parent, false);
                holder = new ViewHolder();
                holder.curationTitle = (TextView) row.findViewById(R.id.tv_curation_title);
                holder.curationCount = (TextView) row.findViewById(R.id.tv_curation_count);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            Curation curation = this.getItem(position);

            holder.curationTitle.setText(curation.getTitle());

            // set RSS Curation unread article count
            holder.curationCount.setText(String.valueOf(unreadManager.getUnreadCount(curation.getId())));

            return row;
        }

        private class ViewHolder {
            TextView curationTitle;
            TextView curationCount;
        }
    }

}
