package com.phicdy.mycuration.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.tracker.GATrackerHelper;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddCurationActivity extends AppCompatActivity {

    private AddCurationFragment wordListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_curation);
        initView();
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
                wordListFragment.onAddMenuClicked();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GATrackerHelper.sendScreen(getTitle().toString());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void initView() {
        wordListFragment = (AddCurationFragment)getSupportFragmentManager().findFragmentById(R.id.fr_curation_condition);
    }

}
