package com.phicdy.mycuration.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.presenter.RegisterFilterPresenter;
import com.phicdy.mycuration.rss.Feed;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.view.fragment.FilterListFragment;
import com.phicdy.mycuration.view.RegisterFilterView;

import java.util.ArrayList;

public class RegisterFilterActivity extends AppCompatActivity implements RegisterFilterView {

    private RegisterFilterPresenter presenter;

    private EditText etTitle;
	private EditText etKeyword;
	private EditText etFilterUrl;
	private TextView tvTargetRss;

    public static final String KEY_SELECTED_FEED = "keySelectedFeed";
	private static final int NEW_FILTER_ID = -1;
    private static final int TARGET_FEED_SELECT_REQUEST = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_filter);

        initView();
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(this);
        int editFilterId = getIntent().getIntExtra(FilterListFragment.KEY_EDIT_FILTER_ID, NEW_FILTER_ID);
        presenter = new RegisterFilterPresenter(dbAdapter, editFilterId);
        presenter.setView(this);

		GATrackerHelper.sendScreen(getTitle().toString());
	}
	
	private void initView() {
		setTitle(R.string.add_filter);

		etKeyword = (EditText) findViewById(R.id.filterKeyword);
		etFilterUrl = (EditText) findViewById(R.id.filterUrl);
		etTitle = (EditText) findViewById(R.id.filterTitle);

		//Set spinner
		tvTargetRss = (TextView) findViewById(R.id.tv_target_rss);
		tvTargetRss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterFilterActivity.this, SelectFilterTargetRssActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(SelectFilterTargetRssActivity.TARGET_RSS, presenter.selectedFeedList());
                intent.putExtras(bundle);
                startActivityForResult(intent, TARGET_FEED_SELECT_REQUEST);
            }
        });
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == TARGET_FEED_SELECT_REQUEST) {
            Bundle bundle = data.getExtras();
            ArrayList<Feed> list = bundle.getParcelableArrayList(KEY_SELECTED_FEED);
            presenter.setSelectedFeedList(list);
        }
    }

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_register_filter, menu);
        return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        presenter.optionItemClicked(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public String filterKeyword() {
        return etKeyword.getText().toString();
    }

    @Override
    public String filterUrl() {
        return etFilterUrl.getText().toString();
    }

    @Override
    public String filterTitle() {
        return etTitle.getText().toString();
    }

    @Override
    public void setFilterTitle(@NonNull String title) {
        etTitle.setText(title);
    }

    @Override
    public void setFilterTargetRss(@NonNull String rss) {
        tvTargetRss.setText(rss);
    }

    @Override
    public void setMultipleFilterTargetRss() {
        tvTargetRss.setText(R.string.multiple_target_rss);
    }

    @Override
    public void resetFilterTargetRss() {
        tvTargetRss.setText(R.string.target_rss);
    }

    @Override
    public void setFilterUrl(@NonNull String url) {
        etFilterUrl.setText(url);
    }

    @Override
    public void setFilterKeyword(@NonNull String keyword) {
        etKeyword.setText(keyword);
    }

    @Override
    public void handleEmptyTitle() {
        Toast.makeText(RegisterFilterActivity.this, R.string.title_empty_error, Toast.LENGTH_SHORT).show();
        GATrackerHelper.sendEvent(getString(R.string.add_new_filter_no_title));
    }

    @Override
    public void handleEmptyCondition() {
        Toast.makeText(RegisterFilterActivity.this, R.string.both_keyword_and_url_empty_error, Toast.LENGTH_SHORT).show();
        GATrackerHelper.sendEvent(getString(R.string.add_new_filter_no_keyword_url));
    }

    @Override
    public void handlePercentOnly() {
        Toast.makeText(RegisterFilterActivity.this, R.string.percent_only_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSaveSuccessToast() {
        Toast.makeText(getApplicationContext(), getString(R.string.filter_saved), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSaveErrorToast() {
        Toast.makeText(getApplicationContext(), getString(R.string.filter_saved_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void trackEdit() {
        GATrackerHelper.sendEvent(getString(R.string.update_filter));
    }

    @Override
    public void trackRegister() {
        GATrackerHelper.sendEvent(getString(R.string.add_new_filter));
    }
}
