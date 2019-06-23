package com.phicdy.mycuration.presentation.view.activity

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.phicdy.mycuration.R
import com.phicdy.mycuration.presentation.view.fragment.ArticlesListFragment
import com.phicdy.mycuration.util.changeTheme
import io.github.inflationx.viewpump.ViewPumpContextWrapper

class ArticleSearchResultActivity : AppCompatActivity(), ArticlesListFragment.OnArticlesListFragmentListener {

    private lateinit var fragment: ArticlesListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_search_result)

        // Set feed id and url from main activity
        fragment = supportFragmentManager
                .findFragmentById(R.id.fr_article_search_result) as ArticlesListFragment
        initToolbar()
    }

    private fun initToolbar() {
        val toolbar = findViewById(R.id.toolbar_article_search_result) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            // Show back arrow icon
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.title = getString(R.string.search_result)
        }
    }

    override fun onResume() {
        super.onResume()
        changeTheme()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }
}
