package com.phicdy.mycuration.presentation.view.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.phicdy.mycuration.BuildConfig;
import com.phicdy.mycuration.R;
import com.phicdy.mycuration.data.db.DatabaseAdapter;
import com.phicdy.mycuration.presentation.presenter.FeedSearchPresenter;
import com.phicdy.mycuration.domain.rss.RssParser;
import com.phicdy.mycuration.domain.rss.UnreadCountManager;
import com.phicdy.mycuration.domain.task.NetworkTaskManager;
import com.phicdy.mycuration.tracker.GATrackerHelper;
import com.phicdy.mycuration.presentation.view.FeedSearchView;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class FeedSearchActivity extends AppCompatActivity implements FeedSearchView {

    private FeedSearchPresenter presenter;
    private SearchView searchView;
    private WebView webView;
    private FloatingActionButton fab;
    private ProgressDialog dialog;

    private static final String SHOWCASE_ID = "searchRssTutorial";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_search);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_feed_search);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show back arrow icon
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.add_rss);
        }

        // Enable JavaScript for Google Search
        webView = (WebView)findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                setSearchViewTextFrom(url);
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);

        NetworkTaskManager manager = NetworkTaskManager.INSTANCE;
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance();
        UnreadCountManager unreadCountManager = UnreadCountManager.getInstance();
        RssParser parser = new RssParser();
        presenter = new FeedSearchPresenter(manager, dbAdapter, unreadCountManager, parser);
        presenter.setView(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = webView.getUrl();
                if (url == null) return;
                presenter.onFabClicked(url);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_feed_search, menu);
        final MenuItem searchMenuItem = menu.findItem(R.id.search_rss);
        searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform final search
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Text change. Apply filter
                return false;
            }
        });

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        SearchView.SearchAutoComplete searchAutoComplete =
                (SearchView.SearchAutoComplete) searchView
                        .findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        searchAutoComplete.setHintTextColor(ContextCompat.getColor(this, R.color.text_primary));


        // Start tutorial at first time
        if (!BuildConfig.DEBUG) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    View view = findViewById(R.id.search_rss);
                    ShowcaseConfig config = new ShowcaseConfig();
                    config.setDelay(500); // half second between each showcase view

                    MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(FeedSearchActivity.this, SHOWCASE_ID);
                    sequence.setConfig(config);

                    // Search tutorial
                    sequence.addSequenceItem(
                            new MaterialShowcaseView.Builder(FeedSearchActivity.this)
                                    .setTarget(view)
                                    .setContentText(R.string.tutorial_search_rss_description)
                                    .setDismissText(R.string.tutorial_next)
                                    .build()
                    );

                    // Add button tutorial
                    sequence.addSequenceItem(
                            new MaterialShowcaseView.Builder(FeedSearchActivity.this)
                                    .setTarget(fab)
                                    .setContentText(R.string.tutorial_add_rss_description)
                                    .setDismissText(R.string.tutorial_close)
                                    .setDismissOnTouch(true)
                                    .build()
                    );

                    // Open software keyboard if tutorial already finished
                    if (sequence.hasFired()) {
                        searchView.setIconified(false);
                    }

                    sequence.start();
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String query = intent.getStringExtra(SearchManager.QUERY);
            if (query == null) return;
            presenter.handle(query);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.pause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK){
            if (webView.canGoBack()) {
                webView.goBack();
            }else {
                finish();
            }
            return true;
        }
        return false;
    }

    @Override
    public void startFeedUrlHookActivity(@NonNull String url) {
        Intent intent = new Intent(FeedSearchActivity.this, FeedUrlHookActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    @Override
    public void showProgressBar() {
        findViewById(R.id.pb_add_url).setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissProgressBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.pb_add_url).setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void load(@NonNull String url) {
        webView.loadUrl(url);
    }

    @Override
    public void showInvalidUrlErrorToast() {
        showToastOnUiThread(R.string.add_rss_error_invalid_url, Toast.LENGTH_SHORT);
        GATrackerHelper.INSTANCE.sendEvent(getString(R.string.add_rss_input_url_error));
    }

    @Override
    public void showGenericErrorToast() {
        showToastOnUiThread(R.string.add_rss_error_generic, Toast.LENGTH_SHORT);
        GATrackerHelper.INSTANCE.sendEvent(getString(R.string.add_rss_input_url_error));
    }

    @Override
    public void showAddFeedSuccessToast() {
        showToastOnUiThread(R.string.add_rss_success, Toast.LENGTH_SHORT);
        GATrackerHelper.INSTANCE.sendEvent(getString(R.string.add_rss_input_url));
    }

    @Override
    public void finishView() {
        finish();
    }

    @Override
    public void setSearchViewTextFrom(@NonNull String url) {
        searchView.setQuery(url, false);
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
