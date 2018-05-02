package com.phicdy.mycuration.presentation.view.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.MenuItemCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
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
import com.phicdy.mycuration.presentation.view.fragment.RssListFragment
import com.phicdy.mycuration.presentation.view.fragment.FilterListFragment
import com.phicdy.mycuration.tracker.GATrackerHelper
import com.phicdy.mycuration.util.PreferenceHelper
import com.phicdy.mycuration.view.activity.SettingActivity
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView

class TopActivity : AppCompatActivity(), RssListFragment.OnFeedListFragmentListener, CurationListFragment.OnCurationListFragmentListener, TopActivityView {
    companion object {
        private const val POSITION_CURATION_FRAGMENT = 0
        private const val POSITION_FEED_FRAGMENT = 1
        private const val POSITION_FILTER_FRAGMENT = 2
        const val FEED_ID = "FEED_ID"
        const val CURATION_ID = "CURATION_ID"
        private const val SHOWCASE_ID = "tutorialAddRss"
    }

    private lateinit var presenter: TopActivityPresenter
    private lateinit var mViewPager: ViewPager
    private lateinit var fab: FloatingActionButton
    private lateinit var llAddCuration: LinearLayout
    private lateinit var llAddRss: LinearLayout
    private lateinit var llAddFilter: LinearLayout
    private lateinit var btnAddCuration: Button
    private lateinit var btnAddRss: Button
    private lateinit var btnAddFilter: Button
    private lateinit var back: FrameLayout

    private lateinit var curationFragment: CurationListFragment
    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top)

        val helper = PreferenceHelper
        val dbAdapter = DatabaseAdapter.getInstance()
        presenter = TopActivityPresenter(helper.launchTab, this, dbAdapter)
        presenter.create()
    }

    override fun initViewPager() {
        curationFragment = CurationListFragment()
        val mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        mViewPager = findViewById(R.id.pager) as ViewPager
        mViewPager.adapter = mSectionsPagerAdapter
        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                setActivityTitle(position)
            }

            override fun onPageSelected(position: Int) {
                setActivityTitle(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        val tabLayout = findViewById(R.id.tab_layout) as TabLayout
        tabLayout.setupWithViewPager(mViewPager)

        // Set icon
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            tab?.setIcon(mSectionsPagerAdapter.getImageResource(i))
        }
    }

    override fun initFab() {
        fab = findViewById(R.id.fab_top) as FloatingActionButton
        fab.setOnClickListener { presenter.fabClicked() }
        back = findViewById(R.id.fl_add_background) as FrameLayout
        back.setOnClickListener {
            presenter.addBackgroundClicked()
        }
        llAddCuration = findViewById(R.id.ll_add_curation) as LinearLayout
        llAddRss = findViewById(R.id.ll_add_rss) as LinearLayout
        llAddFilter = findViewById(R.id.ll_add_filter) as LinearLayout
        llAddCuration.setOnClickListener { presenter.fabCurationClicked() }
        llAddRss.setOnClickListener { presenter.fabRssClicked() }
        llAddFilter.setOnClickListener { presenter.fabFilterClicked() }

        btnAddCuration = findViewById(R.id.btn_add_curation) as Button
        btnAddRss = findViewById(R.id.btn_add_rss) as Button
        btnAddFilter = findViewById(R.id.btn_add_filter) as Button
        btnAddCuration.setOnClickListener { presenter.fabCurationClicked() }
        btnAddRss.setOnClickListener { presenter.fabRssClicked() }
        btnAddFilter.setOnClickListener { presenter.fabFilterClicked() }
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

    private fun setActivityTitle(position: Int) {
        when (position) {
            POSITION_CURATION_FRAGMENT -> title = getString(R.string.curation)
            POSITION_FEED_FRAGMENT -> title = getString(R.string.rss)
            POSITION_FILTER_FRAGMENT -> title = getString(R.string.filter)
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenuItem = menu.findItem(R.id.search_article_top_activity)
        searchView = MenuItemCompat.getActionView(searchMenuItem) as SearchView
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView!!.setOnQueryTextFocusChangeListener { view, queryTextFocused ->
            if (!queryTextFocused) {
                searchMenuItem.collapseActionView()
                searchView!!.setQuery("", false)
            }
        }
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
                val view = findViewById(R.id.fab_top)
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
        GATrackerHelper.sendEvent(getString(R.string.tap_add_rss))
        startActivity(Intent(this@TopActivity, FeedSearchActivity::class.java))
    }

    override fun goToAddCuration() {
        val intent = Intent(applicationContext, AddCurationActivity::class.java)
        startActivity(intent)
        GATrackerHelper.sendEvent(getString(R.string.tap_add_curation))
    }

    override fun goToAddFilter() {
        val intent = Intent(applicationContext, RegisterFilterActivity::class.java)
        startActivity(intent)
        GATrackerHelper.sendEvent(getString(R.string.tap_add_filter))
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

    override fun currentTabPosition(): Int {
        return mViewPager.currentItem
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
        if (position != POSITION_CURATION_FRAGMENT && position != POSITION_FEED_FRAGMENT &&
                position != POSITION_FILTER_FRAGMENT)
            return
        mViewPager.currentItem = position
    }

    private inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? {
            return when (position) {
                POSITION_CURATION_FRAGMENT -> curationFragment
                POSITION_FEED_FRAGMENT -> RssListFragment()
                POSITION_FILTER_FRAGMENT -> FilterListFragment()
                else -> null
            }
        }

        override fun getCount(): Int {
            return 3
        }

        internal fun getImageResource(position: Int): Int {
            when (position) {
                POSITION_CURATION_FRAGMENT -> return R.drawable.tab_curation
                POSITION_FEED_FRAGMENT -> return R.drawable.tab_feed
                POSITION_FILTER_FRAGMENT -> return R.drawable.tab_filter
            }
            return -1
        }
    }

}

