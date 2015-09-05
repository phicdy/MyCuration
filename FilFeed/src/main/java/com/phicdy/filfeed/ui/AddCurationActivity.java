package com.phicdy.filfeed.ui;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.phicdy.filfeed.R;
import com.phicdy.filfeed.db.DatabaseAdapter;
import com.phicdy.filfeed.util.TextUtil;
import com.phicdy.filfeed.util.ToastHelper;

import java.util.ArrayList;

public class AddCurationActivity extends ActionBarActivity {

    private CurationWordListFragment wordListFragment;
    private Button btnAdd;
    private EditText etInput;
    private EditText etName;

    private DatabaseAdapter adapter;
    private Handler handler;
    private MyProgressDialogFragment progressDialog;
    private int editCurationid = NOT_EDIT_CURATION_ID;

    public static final String EDIT_CURATION_ID = "editCurationId";
    public static final int NOT_EDIT_CURATION_ID = -1;
    private static final String INSERT_ERROR_MESSAGE = "insertErrorMessage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_curation);
        initView();
        setAllListener();
        adapter = DatabaseAdapter.getInstance(getApplicationContext());
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                boolean result = (boolean)msg.obj;
                if (result) {
                    ToastHelper.showToast(getApplicationContext(), getString(R.string.curation_added_success), Toast.LENGTH_SHORT);
                    progressDialog.dismiss();
                    finish();
                }else {
                    String errorMessage = msg.getData().getString(INSERT_ERROR_MESSAGE);
                    ToastHelper.showToast(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                    ToastHelper.showToast(getApplicationContext(), getString(R.string.curation_added_error), Toast.LENGTH_SHORT);
                    progressDialog.dismiss();
                }

            }
        };
        editCurationid = getIntent().getIntExtra(EDIT_CURATION_ID, NOT_EDIT_CURATION_ID);
        // Set title for edit
        if (editCurationid != NOT_EDIT_CURATION_ID) {
            setTitle(getString(R.string.title_activity_edit_curation));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_curation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.add_curation:
                insertCurationIntoDb();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initDataForEdit();
    }

    private void initView() {
        wordListFragment = (CurationWordListFragment)getSupportFragmentManager().findFragmentById(R.id.fr_curation_condition);
        btnAdd = (Button)findViewById(R.id.btn_add_word);
        etInput = (EditText)findViewById(R.id.et_curation_word);
        etName = (EditText)findViewById(R.id.et_curation_name);
    }

    // Don't call this method in onCreate()
    private void initDataForEdit() {
        if (editCurationid != NOT_EDIT_CURATION_ID) {
            etName.setText(adapter.getCurationNameById(editCurationid));
            wordListFragment.setWords(adapter.getCurationWords(editCurationid));
        }
    }

    private void setAllListener() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = etInput.getText().toString();
                if (word == null || word.equals("")) {
                    ToastHelper.showToast(getApplicationContext(), getString(R.string.empty_word), Toast.LENGTH_SHORT);
                    return;
                }
                wordListFragment.add(word);
                etInput.setText("");
            }
        });

    }

    private void insertCurationIntoDb() {
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
                    return;
                }
                ArrayList<String> wordList = wordListFragment.getWordList();
                if (wordList == null || wordList.size() == 0) {
                    msg.obj = false;
                    bundle.putString(INSERT_ERROR_MESSAGE, getString(R.string.empty_word_list));
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    return;
                }

                boolean isNew = (editCurationid == NOT_EDIT_CURATION_ID);
                if (isNew && adapter.isExistSameNameCuration(curationName)) {
                    msg.obj = false;
                    bundle.putString(INSERT_ERROR_MESSAGE, getString(R.string.duplicate_curation_name));
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    return;
                }
                boolean result;
                if (isNew) {
                    result = adapter.saveNewCuration(curationName, wordList);
                }else {
                    result = adapter.updateCuration(editCurationid, curationName, wordList);
                }
                if (result) {
                    adapter.adaptCurationToArticles(curationName, wordList);
                }
                msg.obj = true;
                handler.sendMessage(msg);
            }
        }.start();
        progressDialog = MyProgressDialogFragment.newInstance(getString(R.string.adding_curation));
        progressDialog.show(getFragmentManager(), null);
    }
}
