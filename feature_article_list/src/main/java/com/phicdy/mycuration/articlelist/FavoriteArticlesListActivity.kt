package com.phicdy.mycuration.articlelist

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.phicdy.mycuration.feature.util.changeTheme
import com.phicdy.mycuration.feature.util.getThemeColor
import com.phicdy.mycuration.tracker.TrackerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext


class FavoriteArticlesListActivity : AppCompatActivity(), FavoriteArticlesListFragment.OnArticlesListFragmentListener, CoroutineScope {

    companion object {
        private const val TAG_FRAGMENT = "TAG_FRAGMENT"

        fun createIntent(context: Context) =
                Intent(context, FavoriteArticlesListActivity::class.java)
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private lateinit var searchView: SearchView
    private lateinit var fbTitle: String
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_articles_list)

        if (savedInstanceState == null) {
            val fragment = FavoriteArticlesListFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .add(R.id.container, fragment, TAG_FRAGMENT)
                    .commit()
        }

        title = getString(R.string.favorite)
        fbTitle = getString(R.string.ga_favorite)
        TrackerHelper.sendUiEvent(fbTitle)
        initToolbar()
        fab = findViewById(R.id.fab_article_list)
        fab.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) as? FavoriteArticlesListFragment
            fragment?.onFabButtonClicked()
            TrackerHelper.sendButtonEvent(getString(R.string.scroll_article_list))
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
                val intent = Intent(this@FavoriteArticlesListActivity, ArticleSearchResultActivity::class.java)
                intent.action = Intent.ACTION_SEARCH
                intent.putExtra(SearchManager.QUERY, query)
                startActivity(intent)
                return false
            }
        })
        val color = getThemeColor(R.attr.colorPrimary)
        val searchAutoComplete = searchView
                .findViewById(androidx.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete
        searchAutoComplete.setTextColor(color)
        searchAutoComplete.setHintTextColor(color)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.all_read -> {
                TrackerHelper.sendButtonEvent(getString(R.string.read_all_articles))
                val fragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) as? FavoriteArticlesListFragment
                fragment?.handleAllRead()
            }
            android.R.id.home -> finish()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        changeTheme()
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}