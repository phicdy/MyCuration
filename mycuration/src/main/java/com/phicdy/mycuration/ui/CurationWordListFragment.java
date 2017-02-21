package com.phicdy.mycuration.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.util.TextUtil;
import com.phicdy.mycuration.util.ToastHelper;

import java.util.ArrayList;

public class CurationWordListFragment extends Fragment {

    private ListView curationWordListView;
    private Button btnAdd;
    private EditText etInput;
    private EditText etName;
    private CurationWordListAdapter curationWordListAdapter;
    private MyProgressDialogFragment progressDialog;

    private Handler handler;
    private DatabaseAdapter adapter;

    private ArrayList<String> addedWords = new ArrayList<>();

    private int editCurationid = NOT_EDIT_CURATION_ID;

    public static final String EDIT_CURATION_ID = "editCurationId";
    public static final int NOT_EDIT_CURATION_ID = -1;
    private static final String INSERT_ERROR_MESSAGE = "insertErrorMessage";
    private static final String LOG_TAG = "FilFeed.CurationWordList";

    public static CurationWordListFragment newInstance() {
        return new CurationWordListFragment();
    }

    public CurationWordListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editCurationid = getActivity().getIntent().getIntExtra(EDIT_CURATION_ID, NOT_EDIT_CURATION_ID);
        if (editCurationid != NOT_EDIT_CURATION_ID) {
            getActivity().setTitle(getString(R.string.title_activity_edit_curation));
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                boolean result = (boolean)msg.obj;
                if (result) {
                    ToastHelper.showToast(getActivity(), getString(R.string.curation_added_success), Toast.LENGTH_SHORT);
                    progressDialog.dismiss();
                    getActivity().finish();
                }else {
                    String errorMessage = msg.getData().getString(INSERT_ERROR_MESSAGE);
                    ToastHelper.showToast(getActivity(), errorMessage, Toast.LENGTH_SHORT);
                    ToastHelper.showToast(getActivity(), getString(R.string.curation_added_error), Toast.LENGTH_SHORT);
                    progressDialog.dismiss();
                }

            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        initDataForEdit();
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btnAdd = (Button) getActivity().findViewById(R.id.btn_add_word);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = etInput.getText().toString();
                if (word == null || word.equals("")) {
                    ToastHelper.showToast(getActivity(), getString(R.string.empty_word), Toast.LENGTH_SHORT);
                    return;
                }
                add(word);
                etInput.setText("");
            }
        });
        etInput = (EditText) getActivity().findViewById(R.id.et_curation_word);
        etName = (EditText) getActivity().findViewById(R.id.et_curation_name);
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

    // Don't call this method in onCreate()
    private void initDataForEdit() {
        if (editCurationid != NOT_EDIT_CURATION_ID) {
            etName.setText(adapter.getCurationNameById(editCurationid));
            setWords(adapter.getCurationWords(editCurationid));
        }
    }

    public void insertCurationIntoDb() {
        new Thread() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                String curationName = etName.getText().toString();
                if (TextUtil.isEmpty(curationName)) {
                    msg.obj = false;
                    bundle.putString(INSERT_ERROR_MESSAGE, getString(R.string.empty_curation_name));
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    GATrackerHelper.sendEvent(getString(R.string.add_empty_curation_title));
                    return;
                }
                ArrayList<String> wordList = getWordList();
                if (wordList == null || wordList.size() == 0) {
                    msg.obj = false;
                    bundle.putString(INSERT_ERROR_MESSAGE, getString(R.string.empty_word_list));
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    GATrackerHelper.sendEvent(getString(R.string.add_empty_curation_word));
                    return;
                }

                boolean isNew = (editCurationid == NOT_EDIT_CURATION_ID);
                if (isNew && adapter.isExistSameNameCuration(curationName)) {
                    msg.obj = false;
                    bundle.putString(INSERT_ERROR_MESSAGE, getString(R.string.duplicate_curation_name));
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    GATrackerHelper.sendEvent(getString(R.string.add_same_curation_name));
                    return;
                }
                boolean result;
                if (isNew) {
                    result = adapter.saveNewCuration(curationName, wordList);
                    GATrackerHelper.sendEvent(getString(R.string.add_new_curation));
                }else {
                    result = adapter.updateCuration(editCurationid, curationName, wordList);
                    GATrackerHelper.sendEvent(getString(R.string.update_curation));
                }
                if (result) {
                    adapter.adaptCurationToArticles(curationName, wordList);
                }
                msg.obj = true;
                handler.sendMessage(msg);
            }
        }.start();
        progressDialog = MyProgressDialogFragment.newInstance(getString(R.string.adding_curation));
        progressDialog.show(getActivity().getFragmentManager(), null);
    }
}
