package com.phicdy.filfeed.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.phicdy.filfeed.R;
import com.phicdy.filfeed.db.DatabaseAdapter;

import java.util.ArrayList;

public class CurationWordListFragment extends Fragment {

    private ListView curationWordListView;
    private CurationWordListAdapter curationWordListAdapter;

    private DatabaseAdapter adapter;

    private ArrayList<String> addedWords = new ArrayList<>();

    private static final String LOG_TAG = "FilFeed.CurationWordList";

    public static CurationWordListFragment newInstance() {
        return new CurationWordListFragment();
    }

    public CurationWordListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_curation_word_list, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        curationWordListView = (ListView) getActivity().findViewById(R.id.lv_curation_word);
        adapter = DatabaseAdapter.getInstance(getActivity());

        refreshList();

        // Set ListView
        curationWordListAdapter = new CurationWordListAdapter(addedWords, getActivity());
        curationWordListView.setAdapter(curationWordListAdapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void refreshList() {
        curationWordListAdapter = new CurationWordListAdapter(addedWords, getActivity());
        curationWordListView.setAdapter(curationWordListAdapter);
        curationWordListAdapter.notifyDataSetChanged();
    }

    public void add(String word) {
        if (addedWords.contains(word)) {
            Toast.makeText(getActivity(), getString(R.string.duplicate_word), Toast.LENGTH_SHORT).show();
            return;
        }
        addedWords.add(word);
        curationWordListAdapter.notifyDataSetChanged();
    }

    public ArrayList<String> getWordList() {
        return addedWords;
    }

    public void removedWordAtPosition(int position) {
        addedWords.remove(position);
        curationWordListAdapter.notifyDataSetChanged();
    }

    public void setWords(ArrayList<String> words) {
        addedWords = words;
        curationWordListAdapter.notifyDataSetChanged();
    }

    class CurationWordListAdapter extends ArrayAdapter<String> {
        public CurationWordListAdapter(ArrayList<String> words, Context context) {
            super(context, R.layout.curation_word_list, words);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            // Use contentView and setup ViewHolder
            View row = convertView;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.curation_word_list, parent, false);
                holder = new ViewHolder();
                holder.tvWord = (TextView) row.findViewById(R.id.tv_word);
                holder.btnDelete = (Button) row.findViewById(R.id.btn_delete);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            String word = this.getItem(position);
            holder.tvWord.setText(word);
            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removedWordAtPosition(position);
                }
            });

            return row;
        }

        private class ViewHolder {
            TextView tvWord;
            Button btnDelete;
        }
    }

}
