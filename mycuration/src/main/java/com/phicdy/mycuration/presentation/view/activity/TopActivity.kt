package com.phicdy.mycuration.presentation.view.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.phicdy.mycuration.BuildConfig
import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.domain.alarm.AlarmManagerTaskManager
import com.phicdy.mycuration.presentation.presenter.TopActivityPresenter
import com.phicdy.mycuration.presentation.view.TopActivityView
import com.phicdy.mycuration.presentation.view.fragment.CurationListFragment
import com.phicdy.mycuration.presentation.view.fragment.FilterListFragment
import com.phicdy.mycuration.presentation.view.fragment.RssListFragment
import com.phicdy.mycuration.tracker.TrackerHelper
import com.phicdy.mycuration.util.PreferenceHelper
import com.phicdy.mycuration.view.activity.SettingActivity
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView

class TopActivity : AppCompatActivity(), RssListFragment.OnFeedListFragmentListener, CurationListFragment.OnCurationListFragmentListener, TopActivityView {
    companion object {
        const val FEED_ID = "FEED_ID"
        const val CURATION_ID = "CURATION_ID"
        private const val SHOWCASE_ID = "tutorialAddRss"
    }

    private lateinit var presenter: TopActivityPresenter
    private lateinit var fab: FloatingActionButton
    private lateinit var llAddCuration: LinearLayout
    private lateinit var llAddRss: LinearLayout
    private lateinit var llAddFilter: LinearLayout
    private lateinit var btnAddCuration: Button
    private lateinit var btnAddRss: Button
    private lateinit var btnAddFilter: Button
    private lateinit var back: FrameLayout
    private lateinit var navigationView: BottomNavigationView

    private var searchView: SearchView? = null

    private val bottomNavigationListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        replaceFragmentWith(item.itemId)
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top)

        val helper = PreferenceHelper
        val dbAdapter = DatabaseAdapter.getInstance()
        presenter = TopActivityPresenter(helper.launchTab, this, dbAdapter)
        presenter.create()
    }

    override fun initViewPager() {
        navigationView = findViewById(R.id.navigation)
        navigationView.setOnNavigationItemSelectedListener(bottomNavigationListener)
    }

    private fun replaceFragmentWith(menuId: Int) {
        when (menuId) {
            R.id.navigation_curation -> {
                supportActionBar?.title = getString(R.string.curation)
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fr_content, CurationListFragment())
                        .commit()
            }
            R.id.navigation_rss -> {
                supportActionBar?.title = getString(R.string.rss)
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fr_content, RssListFragment())
                        .commit()
            }
            R.id.navigation_filter -> {
                supportActionBar?.title = getString(R.string.filter)
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fr_content, FilterListFragment())
                        .commit()
            }
        }
    }

    override fun initFab() {
        fab = findViewById(R.id.fab_top)
        fab.setOnClickListener { presenter.fabClicked() }
        back = findViewById(R.id.fl_add_background)
        back.setOnClickListener {
            presenter.addBackgroundClicked()
        }
        llAddCuration = findViewById(R.id.ll_add_curation)
        llAddRss = findViewById(R.id.ll_add_rss)
        llAddFilter = findViewById(R.id.ll_add_filter)
        llAddCuration.setOnClickListener { presenter.fabCurationClicked() }
        llAddRss.setOnClickListener { presenter.fabRssClicked() }
        llAddFilter.setOnClickListener { presenter.fabFilterClicked() }

        btnAddCuration = findViewById(R.id.btn_add_curation)
        btnAddRss = findViewById(R.id.btn_add_rss)
        btnAddFilter = findViewById(R.id.btn_add_filter)
        btnAddCuration.setOnClickListener { presenter.fabCurationClicked() }
        btnAddRss.setOnClickListener { presenter.fabRssClicked() }
        btnAddFilter.setOnClickListener { presenter.fabFilterClicked() }
    }

    override fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_top)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun startFabAnimation() {
        back.visibility = View.VISIBLE

        val animation = AnimationUtils.loadAnimation(this, R.anim.fab_rotation)
        fab.startAnimation(animation)

        llAddCuration.visibility = View.VISIBLE
        val fadeInCuration = AnimationUtils.loadAnimation(this, R.anim.fab_fadein_curation)
        llAddCuration.startAnimation(fadeInCuration)

        llAddRss.visibility = View.VISIBLE
        val fadeInRss = AnimationUtils.loadAnimation(this, R.anim.fab_fadein_rss)
        llAddRss.startAnimation(fadeInRss)

        llAddFilter.visibility = View.VISIBLE
        val fadeInFilter = AnimationUtils.loadAnimation(this, R.anim.fab_fadein_filter)
        llAddFilter.startAnimation(fadeInFilter)
    }

    override fun closeAddFab() {
        back.visibility = View.GONE

        val animation = AnimationUtils.loadAnimation(this, R.anim.fab_rotation_back)
        fab.startAnimation(animation)

        val fadeOutCuration = AnimationUtils.loadAnimation(this, R.anim.fab_fadeout_curation)
        llAddCuration.startAnimation(fadeOutCuration)

        val fadeOutRss = AnimationUtils.loadAnimation(this, R.anim.fab_fadeout_rss)
        llAddRss.startAnimation(fadeOutRss)

        val fadeOutFilter = AnimationUtils.loadAnimation(this, R.anim.fab_fadeout_filter)
        llAddFilter.startAnimation(fadeOutFilter)
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenuItem = menu.findItem(R.id.search_article_top_activity)
        searchView = searchMenuItem.actionView as SearchView
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView!!.setOnQueryTextFocusChangeListener { _, queryTextFocused ->
            if (!queryTextFocused) {
                searchMenuItem.collapseActionView()
                searchView!!.setQuery("", false)
            }
        }
        searchView!!.queryHint = getString(R.string.search_article)
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                presenter.queryTextSubmit(query)
                return false
            }
        })
        val searchAutoComplete = searchView!!
                .findViewById(android.support.v7.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete
        searchAutoComplete.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        searchAutoComplete.setHintTextColor(ContextCompat.getColor(this, R.color.text_primary))

        // Start tutorial at first time
        if (!BuildConfig.DEBUG) {
            Handler().post {
                val view = findViewById<View>(R.id.fab_top)
                MaterialShowcaseView.Builder(this@TopActivity)
                        .setTarget(view)
                        .setContentText(
                                R.string.tutorial_go_to_search_rss_description)
                        .setDismissText(R.string.tutorial_next)
                        .singleUse(SHOWCASE_ID)
                        .setListener(object : IShowcaseListener {
                            override fun onShowcaseDisplayed(materialShowcaseView: MaterialShowcaseView) {

                            }

                            override fun onShowcaseDismissed(materialShowcaseView: MaterialShowcaseView) {
                                goToFeedSearch()
                            }
                        })
                        .show()
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        presenter.optionItemClicked(item)
        return super.onOptionsItemSelected(item)
    }

    override fun setAlarmManager() {
        // Start auto update alarmmanager
        val manager = AlarmManagerTaskManager(this)
        val helper = PreferenceHelper
        val intervalSec = helper.autoUpdateIntervalSecond
        manager.setNewAlarm(intervalSec)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (presenter.onKeyDown(keyCode, back.visibility == View.VISIBLE)) return true
        return super.onKeyDown(keyCode, event)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun goToFeedSearch() {
        TrackerHelper.sendButtonEvent(getString(R.string.tap_add_rss))
        startActivity(Intent(this@TopActivity, FeedSearchActivity::class.java))
    }

    override fun goToAddCuration() {
        val intent = Intent(applicationContext, AddCurationActivity::class.java)
        startActivity(intent)
        TrackerHelper.sendButtonEvent(getString(R.string.tap_add_curation))
    }

    override fun goToAddFilter() {
        val intent = Intent(applicationContext, RegisterFilterActivity::class.java)
        startActivity(intent)
        TrackerHelper.sendButtonEvent(getString(R.string.tap_add_filter))
    }

    override fun goToSetting() {
        startActivity(Intent(applicationContext, SettingActivity::class.java))
    }

    override fun goToArticleSearchResult(query: String) {
        val intent = Intent(this@TopActivity, ArticleSearchResultActivity::class.java)
        intent.action = Intent.ACTION_SEARCH
        intent.putExtra(SearchManager.QUERY, query)
        startActivity(intent)
    }

    override fun onListClicked(feedId: Int) {
        val intent = Intent(applicationContext, ArticlesListActivity::class.java)
        intent.putExtra(FEED_ID, feedId)
        startActivity(intent)
    }

    override fun onAllUnreadClicked() {
        val intent = Intent(applicationContext, ArticlesListActivity::class.java)
        startActivity(intent)
    }

    override fun onCurationListClicked(curationId: Int) {
        val intent = Intent()
        intent.setClass(applicationContext, ArticlesListActivity::class.java)
        intent.putExtra(CURATION_ID, curationId)
        startActivity(intent)
    }

    override fun closeSearchView() {
        if (searchView != null) {
            searchView!!.onActionViewCollapsed()
            searchView!!.setQuery("", false)
        }
    }

    override fun changeTab(position: Int) {
        when(position) {
            PreferenceHelper.LAUNCH_CURATION -> navigationView.selectedItemId = R.id.navigation_curation
            PreferenceHelper.LAUNCH_RSS -> navigationView.selectedItemId = R.id.navigation_rss
        }
    }
}

