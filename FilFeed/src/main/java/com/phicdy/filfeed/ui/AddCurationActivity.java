package com.phicdy.filfeed.ui;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_curation);
        initView();
        setAllListener();
        adapter = DatabaseAdapter.getInstance(getApplicationContext());
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
                if (insertCurationIntoDb()) {
                    ToastHelper.showToast(getApplicationContext(), getString(R.string.curation_added_success), Toast.LENGTH_SHORT);
                    finish();
                }else {
                    ToastHelper.showToast(getApplicationContext(), getString(R.string.curation_added_error), Toast.LENGTH_SHORT);
                }
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        wordListFragment = (CurationWordListFragment)getSupportFragmentManager().findFragmentById(R.id.fr_curation_condition);
        btnAdd = (Button)findViewById(R.id.btn_add_word);
        etInput = (EditText)findViewById(R.id.et_curation_word);
        etName = (EditText)findViewById(R.id.et_curation_name);
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

    private boolean insertCurationIntoDb() {
        String curationName = etName.getText().toString();
        if (TextUtil.isEmpty(curationName)) {
            ToastHelper.showToast(getApplicationContext(), getString(R.string.empty_curation_name), Toast.LENGTH_SHORT);
            return false;
        }
        ArrayList<String> wordList = wordListFragment.getWordList();
        if (wordList == null || wordList.size() == 0) {
            ToastHelper.showToast(getApplicationContext(), getString(R.string.empty_word_list), Toast.LENGTH_SHORT);
            return false;
        }
        if (adapter.isExistSameNameCuration(curationName)) {
            ToastHelper.showToast(getApplicationContext(), getString(R.string.duplicate_curation_name), Toast.LENGTH_SHORT);
            return false;
        }
        return adapter.saveNewCuration(curationName, wordList);
    }
}
