package com.phicdy.mycuration.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
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
import com.phicdy.mycuration.presenter.AddCurationPresenter;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.util.ToastHelper;
import com.phicdy.mycuration.view.AddCurationView;

import java.util.ArrayList;

public class AddCurationFragment extends Fragment implements AddCurationView {

    private ListView curationWordListView;
    private Button btnAdd;
    private EditText etInput;
    private EditText etName;
    private CurationWordListAdapter curationWordListAdapter;
    private MyProgressDialogFragment progressDialog;

    private Handler handler;
    private DatabaseAdapter adapter;

    private ArrayList<String> addedWords = new ArrayList<>();


    private AddCurationPresenter presenter;
    public static final String EDIT_CURATION_ID = "editCurationId";
    private static final String LOG_TAG = "FilFeed.CurationWordList";

    public AddCurationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = DatabaseAdapter.getInstance(getActivity());
        presenter = new AddCurationPresenter(adapter);
        presenter.setView(this);
        presenter.create();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                boolean result = (boolean)msg.obj;
                String errorMessage = msg.getData().getString(AddCurationPresenter.INSERT_ERROR_MESSAGE);
                presenter.handleInsertResultMessage(result, errorMessage);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
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
                presenter.onAddWordButtonClicked();
            }
        });
        etInput = (EditText) getActivity().findViewById(R.id.et_curation_word);
        etName = (EditText) getActivity().findViewById(R.id.et_curation_name);
        curationWordListView = (ListView) getActivity().findViewById(R.id.lv_curation_word);

        refreshList();
    }

    @Override
    public void refreshList() {
        curationWordListAdapter = new CurationWordListAdapter(addedWords, getActivity());
        curationWordListView.setAdapter(curationWordListAdapter);
        curationWordListAdapter.notifyDataSetChanged();
    }

    @Override
    public void addWord(String word) {
        if (addedWords.contains(word)) {
            Toast.makeText(getActivity(), getString(R.string.duplicate_word), Toast.LENGTH_SHORT).show();
            return;
        }
        addedWords.add(word);
        curationWordListAdapter.notifyDataSetChanged();
    }

    @Override
    public ArrayList<String> wordList() {
        return addedWords;
    }

    public void removedWordAtPosition(int position) {
        addedWords.remove(position);
        curationWordListAdapter.notifyDataSetChanged();
    }

    @Override
    public void setWords(ArrayList<String> words) {
        addedWords = words;
        curationWordListAdapter.notifyDataSetChanged();
    }

    @Override
    public int editCurationId() {
        return getActivity().getIntent().getIntExtra(EDIT_CURATION_ID, AddCurationPresenter.NOT_EDIT_CURATION_ID);
    }

    @Override
    public String inputWord() {
        return etInput.getText().toString();
    }

    @Override
    public String curationName() {
        return etName.getText().toString();
    }

    @Override
    public void setCurationName(String name) {
        etName.setText(name);
    }

    @Override
    public void resetInputWord() {
        etInput.setText("");
    }

    @Override
    public void setTitleForEdit() {
        getActivity().setTitle(getString(R.string.title_activity_edit_curation));
    }

    @Override
    public void handleEmptyCurationNameError() {
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        msg.obj = false;
        bundle.putString(AddCurationPresenter.INSERT_ERROR_MESSAGE, getString(R.string.empty_curation_name));
        msg.setData(bundle);
        handler.sendMessage(msg);
        GATrackerHelper.sendEvent(getString(R.string.add_empty_curation_title));
    }

    @Override
    public void handleEmptyWordError() {
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        msg.obj = false;
        bundle.putString(AddCurationPresenter.INSERT_ERROR_MESSAGE, getString(R.string.empty_word_list));
        msg.setData(bundle);
        handler.sendMessage(msg);
        GATrackerHelper.sendEvent(getString(R.string.add_empty_curation_word));
    }

    @Override
    public void handleSameNameCurationError() {
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        msg.obj = false;
        bundle.putString(AddCurationPresenter.INSERT_ERROR_MESSAGE, getString(R.string.duplicate_curation_name));
        msg.setData(bundle);
        handler.sendMessage(msg);
        GATrackerHelper.sendEvent(getString(R.string.add_same_curation_name));
    }

    @Override
    public void handleAddSuccess() {
        Message msg = Message.obtain();
        msg.obj = true;
        handler.sendMessage(msg);
        GATrackerHelper.sendEvent(getString(R.string.add_new_curation));
    }

    @Override
    public void handleEditSuccess() {
        Message msg = Message.obtain();
        msg.obj = true;
        handler.sendMessage(msg);
        GATrackerHelper.sendEvent(getString(R.string.update_curation));
    }

    @Override
    public void showSuccessToast() {
        ToastHelper.showToast(getActivity(), getString(R.string.curation_added_success), Toast.LENGTH_SHORT);
    }

    @Override
    public void showErrorToast() {
        ToastHelper.showToast(getActivity(), getString(R.string.curation_added_error), Toast.LENGTH_SHORT);
    }

    @Override
    public void showWordEmptyErrorToast() {
        ToastHelper.showToast(getActivity(), getString(R.string.empty_word), Toast.LENGTH_SHORT);
    }

    @Override
    public void showToast(String text) {
        ToastHelper.showToast(getActivity(), text, Toast.LENGTH_SHORT);
    }

    @Override
    public void showProgressDialog() {
        progressDialog = MyProgressDialogFragment.newInstance(getString(R.string.adding_curation));
        progressDialog.show(getActivity().getFragmentManager(), null);
    }

    @Override
    public void dismissProgressDialog() {
        progressDialog.dismiss();
    }

    @Override
    public void finish() {
        getActivity().finish();
    }

    class CurationWordListAdapter extends ArrayAdapter<String> {
        CurationWordListAdapter(ArrayList<String> words, Context context) {
            super(context, R.layout.curation_word_list, words);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
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

    public void onAddMenuClicked() {
        showProgressDialog();
        new Thread() {
            @Override
            public void run() {
                presenter.onAddMenuClicked();
            }
        }.start();
    }
}
