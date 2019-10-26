package com.phicdy.mycuration.curatedarticlelist

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.phicdy.mycuration.articlelist.ArticleSearchResultActivity
import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.feature.util.changeTheme
import com.phicdy.mycuration.feature.util.getThemeColor
import com.phicdy.mycuration.feature_curated_article_list.R
import com.phicdy.mycuration.tracker.TrackerHelper
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class CuratedArticlesListActivity : AppCompatActivity(), CuratedArticlesListFragment.OnArticlesListFragmentListener {

    companion object {
        private const val DEFAULT_CURATION_ID = -1
        private const val TAG_FRAGMENT = "TAG_FRAGMENT"
        private const val CURATION_ID = "CURATION_ID"

        fun createIntent(context: Context, curationId: Int) =
                Intent(context, CuratedArticlesListActivity::class.java).apply {
                    putExtra(CURATION_ID, curationId)
                }
    }

    private lateinit var searchView: SearchView
    private lateinit var fbTitle: String
    private lateinit var fab: FloatingActionButton

    private val curationRepository: CurationRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_curated_articles_list)

        // Set feed id and url from main activity
        val intent = intent
        val curationId = intent.getIntExtra(CURATION_ID, DEFAULT_CURATION_ID)

        if (savedInstanceState == null) {
            val fragment = CuratedArticlesListFragment.newInstance(curationId)
            supportFragmentManager.beginTransaction()
                    .add(R.id.container, fragment, TAG_FRAGMENT)
                    .commit()
        }

        lifecycleScope.launch {
            title = curationRepository.getCurationNameById(curationId)
            fbTitle = getString(R.string.curation)
            TrackerHelper.sendUiEvent(fbTitle)
            initToolbar()
            fab = findViewById(R.id.fab_article_list)
            fab.setOnClickListener {
                val fragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) as? CuratedArticlesListFragment
                fragment?.onFabButtonClicked()
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
        inflater.inflate(R.menu.menu_curated_articles_list, menu)
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
                val intent = Intent(this@CuratedArticlesListActivity, ArticleSearchResultActivity::class.java)
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
                val fragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) as? CuratedArticlesListFragment
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
}