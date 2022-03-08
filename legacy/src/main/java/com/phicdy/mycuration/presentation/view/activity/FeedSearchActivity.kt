package com.phicdy.mycuration.presentation.view.activity

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.phicdy.mycuration.feature.util.changeTheme
import com.phicdy.mycuration.feature.util.getThemeColor
import com.phicdy.mycuration.feedurlhook.FeedUrlHookActivity
import com.phicdy.mycuration.legacy.BuildConfig
import com.phicdy.mycuration.legacy.R
import com.phicdy.mycuration.presentation.presenter.FeedSearchPresenter
import com.phicdy.mycuration.presentation.view.FeedSearchView
import com.phicdy.mycuration.tracker.TrackerHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class FeedSearchActivity : AppCompatActivity(), FeedSearchView, CoroutineScope {

    companion object {
        private const val SHOWCASE_ID = "searchRssTutorial"
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    @Inject
    lateinit var presenter: FeedSearchPresenter
    private lateinit var searchView: SearchView
    private lateinit var webView: WebView
    private lateinit var fab: FloatingActionButton

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_search)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_feed_search)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            // Show back arrow icon
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setTitle(R.string.add_rss)
        }

        // Enable JavaScript for Google Search
        webView = findViewById(R.id.webview)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (url == null) return
                setSearchViewTextFrom(url)
            }
        }
        webView.settings.javaScriptEnabled = true

        fab = findViewById(R.id.fab)
        fab.setOnClickListener(View.OnClickListener {
            val url = webView.url ?: return@OnClickListener
            presenter.onFabClicked(url)
        })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_feed_search, menu)
        val searchMenuItem = menu.findItem(R.id.search_rss)
        searchView = searchMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Perform final search
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // Text change. Apply filter
                return false
            }
        })

        val color = getThemeColor(R.attr.colorPrimary)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(componentName))
        val searchAutoComplete = searchView
                .findViewById(androidx.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete
        searchAutoComplete.setTextColor(color)
        searchAutoComplete.setHintTextColor(color)


        // Start tutorial at first time
        if (!BuildConfig.DEBUG) {
            Handler().post {
                val view = findViewById<View>(R.id.search_rss)
                val config = ShowcaseConfig()
                config.delay = 500 // half second between each showcase view

                val sequence = MaterialShowcaseSequence(this@FeedSearchActivity, SHOWCASE_ID)
                sequence.setConfig(config)

                // Search tutorial
                sequence.addSequenceItem(
                        MaterialShowcaseView.Builder(this@FeedSearchActivity)
                                .setTarget(view)
                                .setContentText(R.string.tutorial_search_rss_description)
                                .setDismissText(R.string.tutorial_next)
                                .build()
                )

                // Add button tutorial
                sequence.addSequenceItem(
                        MaterialShowcaseView.Builder(this@FeedSearchActivity)
                                .setTarget(fab)
                                .setContentText(R.string.tutorial_add_rss_description)
                                .setDismissText(R.string.tutorial_close)
                                .setDismissOnTouch(true)
                                .build()
                )

                // Open software keyboard if tutorial already finished
                if (sequence.hasFired()) {
                    searchView.isIconified = false
                }

                sequence.start()
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY) ?: return
            launch { presenter.handle(query) }
        }
    }

    override fun onResume() {
        super.onResume()
        changeTheme()
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                finish()
            }
            return true
        }
        return false
    }

    override fun startFeedUrlHookActivity(url: String) {
        val intent = Intent(this@FeedSearchActivity, FeedUrlHookActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    override fun showProgressBar() {
        findViewById<View>(R.id.pb_add_url).visibility = View.VISIBLE
    }

    override fun dismissProgressBar() {
        runOnUiThread { findViewById<View>(R.id.pb_add_url).visibility = View.GONE }
    }

    override fun load(url: String) {
        webView.loadUrl(url)
    }

    override fun showInvalidUrlErrorToast() {
        showToastOnUiThread(R.string.add_rss_error_invalid_url, Toast.LENGTH_SHORT)
    }

    override fun showGenericErrorToast() {
        showToastOnUiThread(R.string.add_rss_error_generic, Toast.LENGTH_SHORT)
    }

    override fun showAddFeedSuccessToast() {
        showToastOnUiThread(R.string.add_rss_success, Toast.LENGTH_SHORT)
        TrackerHelper.sendButtonEvent(getString(R.string.add_rss_input_url))
    }

    override fun finishView() {
        finish()
    }

    override fun setSearchViewTextFrom(url: String) {
        searchView.setQuery(url, false)
    }

    override fun trackFailedUrl(url: String) {
        TrackerHelper.sendFailedParseUrl(getString(R.string.add_rss_input_url_error), url)
    }

    @UiThread
    private fun showToastOnUiThread(@StringRes res: Int, toastLength: Int) {
        runOnUiThread { Toast.makeText(applicationContext, res, toastLength).show() }
    }

    @Module
    @InstallIn(ActivityComponent::class)
    object FeedSearchModule {

        @ActivityScoped
        @Provides
        fun provideFeedSearchView(@ActivityContext activity: Context): FeedSearchView =
                activity as FeedSearchView
    }
}
