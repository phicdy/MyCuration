package com.phicdy.mycuration.view.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import com.phicdy.mycuration.R;
import com.phicdy.mycuration.db.DatabaseAdapter;
import com.phicdy.mycuration.presenter.FeedUrlHookPresenter;
import com.phicdy.mycuration.rss.UnreadCountManager;
import com.phicdy.mycuration.task.NetworkTaskManager;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.view.FeedUrlHookView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FeedUrlHookActivity extends Activity implements FeedUrlHookView {

    private FeedUrlHookPresenter presenter;
	private ProgressDialog dialog;
	private BroadcastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed_url_hook);

        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(this);
        UnreadCountManager unreadCountManager = UnreadCountManager.getInstance(this);
        NetworkTaskManager networkTaskManager = NetworkTaskManager.getInstance(this);
		presenter = new FeedUrlHookPresenter(dbAdapter, unreadCountManager, networkTaskManager);
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
        GATrackerHelper.sendScreen(getString(R.string.add_rss_from_intent));
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
	public void showProgressDialog() {
		dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage(getString(R.string.adding_rss));
		dialog.show();
	}

    @Override
    public void dismissProgressDialog() {
        if (dialog != null) dialog.dismiss();
    }

    @Override
    public void registerFinishAddReceiver() {
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String feedUrl = intent.getStringExtra(NetworkTaskManager.ADDED_FEED_URL);
                int errorReason = intent.getIntExtra(NetworkTaskManager.ADD_FEED_ERROR_REASON,
                        NetworkTaskManager.REASON_NOT_FOUND);
                presenter.handleFinish(action, feedUrl, errorReason);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(NetworkTaskManager.FINISH_ADD_FEED);
        filter.addAction(NetworkTaskManager.FINISH_UPDATE_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    public void unregisterFinishAddReceiver() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public void showInvalidUrlErrorToast() {
        Toast.makeText(getApplicationContext(),
                R.string.add_rss_error_invalid_url,
                Toast.LENGTH_SHORT).show();
        GATrackerHelper.sendEvent(getString(R.string.add_rss_from_intent_error));
    }

    @Override
    public void showGenericErrorToast() {
        Toast.makeText(getApplicationContext(),
                R.string.add_rss_error_generic,
                Toast.LENGTH_SHORT).show();
        GATrackerHelper.sendEvent(getString(R.string.add_rss_from_intent_error));
    }

    @Override
    public void finishView() {
        finish();
    }

    @Override
    public void showSuccessToast() {
        Toast.makeText(getApplicationContext(),
                R.string.add_rss_success,
                Toast.LENGTH_SHORT).show();
    }
}
