package com.phicdy.mycuration.presentation.view.fragment;

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
import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.presentation.presenter.CurationListPresenter;
import com.phicdy.mycuration.data.rss.Curation;
import com.phicdy.mycuration.data.rss.UnreadCountManager;
import com.phicdy.mycuration.presentation.view.CurationListView;
import com.phicdy.mycuration.presentation.view.activity.AddCurationActivity;

import java.util.ArrayList;


public class CurationListFragment extends Fragment implements CurationListView {

    private CurationListPresenter presenter;
    private CurationListAdapter curationListAdapter;
    private OnCurationListFragmentListener mListener;
    private ListView curationListView;
    private TextView emptyView;

    private UnreadCountManager unreadManager;

    private static final int EDIT_CURATION_MENU_ID = 1;
    private static final int DELETE_CURATION_MENU_ID = 2;

    public CurationListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance();
        presenter = new CurationListPresenter(dbAdapter);
        presenter.setView(this);
        unreadManager = UnreadCountManager.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
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
                Curation editCuration = curationListAdapter.getItem(info.position);
                if (editCuration != null) {
                    presenter.onCurationEditClicked(editCuration.getId());
                }
                return true;
            case DELETE_CURATION_MENU_ID:
                Curation curation = curationListAdapter.getItem(info.position);
                presenter.onCurationDeleteClicked(curation);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void setAllListener() {
        curationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Curation curation = curationAt(position);
                if (curation != null) {
                    mListener.onCurationListClicked(curation.getId());
                }
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
        emptyView = (TextView)getActivity().findViewById(R.id.emptyView_curation);
        setAllListener();
        presenter.activityCreated();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void startEditCurationActivity(int editCurationId) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), AddCurationActivity.class);
        intent.putExtra(AddCurationFragment.EDIT_CURATION_ID, editCurationId);
        startActivity(intent);
    }

    @Override
    public void setNoRssTextToEmptyView() {
        emptyView.setText(R.string.no_rss_message);
    }

    @Override
    public void setEmptyViewToList() {
        curationListView.setEmptyView(emptyView);
    }

    @Override
    public void registerContextMenu() {
        registerForContextMenu(curationListView);
    }

    @Override
    public void initListBy(ArrayList<Curation> curations) {
        curationListAdapter = new CurationListAdapter(curations, getActivity());
        curationListView.setAdapter(curationListAdapter);
        curationListAdapter.notifyDataSetChanged();
    }

    @Override
    public void delete(Curation curation) {
        curationListAdapter.remove(curation);
        curationListAdapter.notifyDataSetChanged();
    }

    @Override
    public int size() {
        if (curationListAdapter == null) return 0;
        return curationListAdapter.getCount();
    }

    @Override
    public Curation curationAt(int position) {
        if (curationListAdapter == null || position > curationListAdapter.getCount())
            return null;
        return curationListAdapter.getItem(position);
    }

    public interface OnCurationListFragmentListener {
        void onCurationListClicked(int curationId);
    }

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
