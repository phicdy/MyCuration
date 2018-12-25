package com.phicdy.mycuration.presentation.view.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.presentation.view.fragment.ArticlesListFragment
import com.phicdy.mycuration.tracker.TrackerHelper
import com.phicdy.mycuration.util.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class ArticlesListActivity : AppCompatActivity(), ArticlesListFragment.OnArticlesListFragmentListener, CoroutineScope {

    companion object {
        private const val DEFAULT_CURATION_ID = -1
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private lateinit var searchView: SearchView
    private lateinit var fbTitle: String
    private lateinit var fragment: ArticlesListFragment
    private lateinit var fab: FloatingActionButton

    private val rssRepository: RssRepository by inject()
    private val curationRepository: CurationRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articles_list)

        fragment = supportFragmentManager.findFragmentById(R.id.fr_article_list) as ArticlesListFragment

        // Set feed id and url from main activity
        val intent = intent
        val feedId = intent.getIntExtra(TopActivity.FEED_ID, Feed.ALL_FEED_ID)
        val curationId = intent.getIntExtra(TopActivity.CURATION_ID, DEFAULT_CURATION_ID)
        intent.putExtra(TopActivity.FEED_ID, feedId)

        launch {
            when {
                curationId != DEFAULT_CURATION_ID -> {
                    // Curation
                    title = curationRepository.getCurationNameById(curationId)
                    fbTitle = getString(R.string.curation)
                }
                feedId == Feed.ALL_FEED_ID -> {
                    // All article
                    title = getString(R.string.all)
                    fbTitle = getString(R.string.all)
                }
                else -> {
                    // Select a feed
                    val prefMgr = PreferenceHelper
                    prefMgr.setSearchFeedId(feedId)
                    val selectedFeed = rssRepository.getFeedById(feedId)
                    title = selectedFeed?.title
                    fbTitle = getString(R.string.ga_not_all_title)
                }
            }
            TrackerHelper.sendUiEvent(fbTitle)
            initToolbar()
            fab = findViewById(R.id.fab_article_list)
            fab.setOnClickListener {
                fragment.onFabButtonClicked()
                TrackerHelper.sendButtonEvent(getString(R.string.scroll_article_list))
            }
        }
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_article_list)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            // Show back arrow icon
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.title = title
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_article, menu)
        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenuItem = menu.findItem(R.id.search_article)
        searchView = searchMenuItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(componentName))
        searchView.queryHint = getString(R.string.search_article)
        searchView.setOnQueryTextFocusChangeListener { _, queryTextFocused ->
            if (!queryTextFocused) {
                searchMenuItem.collapseActionView()
                searchView.setQuery("", false)
            }
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query == null) return false
                val intent = Intent(this@ArticlesListActivity, ArticleSearchResultActivity::class.java)
                intent.action = Intent.ACTION_SEARCH
                intent.putExtra(SearchManager.QUERY, query)
                startActivity(intent)
                return false
            }
        })
        val searchAutoComplete = searchView
                .findViewById(androidx.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete
        searchAutoComplete.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        searchAutoComplete.setHintTextColor(ContextCompat.getColor(this, R.color.text_primary))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.all_read -> {
                TrackerHelper.sendButtonEvent(getString(R.string.read_all_articles))
                fragment.handleAllRead()
            }
            android.R.id.home -> finish()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}