package com.phicdy.mycuration.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.widget.Toast;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.presentation.presenter.FeedUrlHookPresenter;
import com.phicdy.mycuration.data.rss.RssParser;
import com.phicdy.mycuration.data.rss.UnreadCountManager;
import com.phicdy.mycuration.domain.task.NetworkTaskManager;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.presentation.view.FeedUrlHookView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FeedUrlHookActivity extends Activity implements FeedUrlHookView {

    private FeedUrlHookPresenter presenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed_url_hook);

        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance();
        UnreadCountManager unreadCountManager = UnreadCountManager.getInstance();
        NetworkTaskManager networkTaskManager = NetworkTaskManager.INSTANCE;
        RssParser parser = new RssParser();
		presenter = new FeedUrlHookPresenter(dbAdapter, unreadCountManager, networkTaskManager, parser);
		presenter.setView(this);

		Intent intent = getIntent();
		final String action = intent.getAction();
        String url = null;
        if (action.equals(Intent.ACTION_VIEW)) {
            url = intent.getDataString();
        }else if (action.equals(Intent.ACTION_SEND)) {
            // For Chrome
            Bundle extras = getIntent().getExtras();
            CharSequence urlChar = extras.getCharSequence(Intent.EXTRA_TEXT);
            if (urlChar != null) {
                url = urlChar.toString();
            }
        }
        if (url != null) {
            presenter.handle(action, url);
        }
        GATrackerHelper.INSTANCE.sendScreen(getString(R.string.add_rss_from_intent));
	}

	@Override
	protected void onPause() {
		super.onPause();
        presenter.pause();
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}

    @Override
    public void showInvalidUrlErrorToast() {
        showToastOnUiThread(R.string.add_rss_error_invalid_url, Toast.LENGTH_SHORT);
        GATrackerHelper.INSTANCE.sendEvent(getString(R.string.add_rss_from_intent_error));
    }

    @Override
    public void showGenericErrorToast() {
        showToastOnUiThread(R.string.add_rss_error_generic, Toast.LENGTH_SHORT);
        GATrackerHelper.INSTANCE.sendEvent(getString(R.string.add_rss_from_intent_error));
    }

    @Override
    public void finishView() {
        finish();
    }

    @Override
    public void showSuccessToast() {
        showToastOnUiThread(R.string.add_rss_success, Toast.LENGTH_SHORT);
    }

    @UiThread
    private void showToastOnUiThread(@StringRes final int res, final int toastLength) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), res, toastLength).show();
            }
        });
    }
}
