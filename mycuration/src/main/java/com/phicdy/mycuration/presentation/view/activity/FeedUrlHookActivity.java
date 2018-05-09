package com.phicdy.mycuration.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.widget.Toast;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.presentation.presenter.FeedUrlHookPresenter;
import com.phicdy.mycuration.domain.rss.RssParser;
import com.phicdy.mycuration.domain.rss.UnreadCountManager;
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
        Intent intent = getIntent();
        final String action = intent.getAction() == null ? "": intent.getAction();
        final String dataString = intent.getDataString() == null ? "": intent.getDataString();
        final CharSequence extrasText = intent.getExtras() == null ? "": intent.getExtras().getCharSequence(Intent.EXTRA_TEXT, "");
		presenter = new FeedUrlHookPresenter(action, dataString, extrasText,
                dbAdapter, unreadCountManager, networkTaskManager, parser);
		presenter.setView(this);
		presenter.create();
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
    }

    @Override
    public void showGenericErrorToast() {
        showToastOnUiThread(R.string.add_rss_error_generic, Toast.LENGTH_SHORT);
    }

    @Override
    public void finishView() {
        finish();
    }

    @Override
    public void trackFailedUrl(@NonNull String url) {
        GATrackerHelper.INSTANCE.sendEvent(getString(R.string.add_rss_from_intent_error), url);
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
