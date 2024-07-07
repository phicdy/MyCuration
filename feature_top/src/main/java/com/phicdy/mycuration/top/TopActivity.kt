package com.phicdy.mycuration.top

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.phicdy.feature_register_filter.RegisterFilterActivity
import com.phicdy.mycuration.articlelist.ArticleSearchResultActivity
import com.phicdy.mycuration.curatedarticlelist.CuratedArticlesListActivity
import com.phicdy.mycuration.curationlist.CurationListFragment
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.domain.alarm.AlarmManagerTaskManager
import com.phicdy.mycuration.feature.addcuration.AddCurationActivity
import com.phicdy.mycuration.feature.util.changeTheme
import com.phicdy.mycuration.feature.util.getThemeColor
import com.phicdy.mycuration.feedsearch.FeedSearchActivity
import com.phicdy.mycuration.filterlist.FilterListFragment
import com.phicdy.mycuration.rss.RssListFragment
import com.phicdy.mycuration.setting.SettingActivity
import com.phicdy.mycuration.tracker.TrackerHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import javax.inject.Inject

@AndroidEntryPoint
class TopActivity :
    AppCompatActivity(),
    CurationListFragment.OnCurationListFragmentListener {

    companion object {
        private const val SHOWCASE_ID = "tutorialAddRss"
        private const val FRAGMENT_TAG = "FRAGMENT_TAG"
    }

    @Inject
    lateinit var initializeTopActionCreator: InitializeTopActionCreator

    @Inject
    lateinit var checkReviewRequestActionCreator: CheckReviewRequestActionCreator

    @Inject
    lateinit var closeRateDialogActionCreator: CloseRateDialogActionCreator

    @Inject
    lateinit var helper: PreferenceHelper

    private val topStateStore: TopStateStore by viewModels()

    private lateinit var fab: FloatingActionButton
    private lateinit var fabAddCuration: FloatingActionButton
    private lateinit var fabAddRss: FloatingActionButton
    private lateinit var fabAddFilter: FloatingActionButton
    private lateinit var llAddCuration: LinearLayout
    private lateinit var llAddRss: LinearLayout
    private lateinit var llAddFilter: LinearLayout
    private lateinit var btnAddCuration: Button
    private lateinit var btnAddRss: Button
    private lateinit var btnAddFilter: Button
    private lateinit var back: FrameLayout
    private lateinit var navigationView: BottomNavigationView

    private var searchView: SearchView? = null

    private val openFeedSearch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)) {
                is RssListFragment -> fragment.reload()
            }
        }

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if (back.visibility == View.VISIBLE) {
                closeAddFab()
                isEnabled = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top)
        initViewPager()
        initFab()
        initToolbar()
        setAlarmManager()
        if (savedInstanceState == null) {
            changeTab(PreferenceHelper.launchTab)
        } else {
            val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
            when (fragment) {
                is CurationListFragment -> supportActionBar?.title = getString(R.string.curation)
                is RssListFragment -> supportActionBar?.title = getString(R.string.rss)
                is FilterListFragment -> supportActionBar?.title = getString(R.string.filter)
            }

        }
        lifecycleScope.launch {
            initializeTopActionCreator.run()
        }

        topStateStore.state.observe(this) { state ->
            if (state.showRateDialog) {
                showRateDialog()
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    private fun initViewPager() {
        navigationView = findViewById(R.id.navigation)
        navigationView.setOnNavigationItemSelectedListener { item ->
            replaceFragmentWith(item.itemId)
            true
        }
    }

    private fun replaceFragmentWith(menuId: Int) {
        when (menuId) {
            R.id.navigation_curation -> {
                supportActionBar?.title = getString(R.string.curation)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fr_content, CurationListFragment(), FRAGMENT_TAG)
                    .commit()
            }

            R.id.navigation_rss -> {
                supportActionBar?.title = getString(R.string.rss)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fr_content, RssListFragment(), FRAGMENT_TAG)
                    .commit()
            }

            R.id.navigation_filter -> {
                supportActionBar?.title = getString(R.string.filter)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fr_content, FilterListFragment(), FRAGMENT_TAG)
                    .commit()
            }
        }
    }

    private fun initFab() {
        fun onAddCurationClicked() {
            closeAddFab()
            val num = topStateStore.state.value?.numOfRss
            if (num == 0L) {
                goToFeedSearch()
                return
            }
            goToAddCuration()
        }

        fun onAddRssClicked() {
            closeAddFab()
            goToFeedSearch()
        }

        fun onAddFilterClicked() {
            closeAddFab()
            val num = topStateStore.state.value?.numOfRss
            if (num == 0L) {
                goToFeedSearch()
                return
            }
            goToAddFilter()
        }

        fab = findViewById(R.id.fab_top)
        fab.setOnClickListener { startFabAnimation() }

        back = findViewById(R.id.fl_add_background)
        back.setOnClickListener {
            closeAddFab()
        }

        llAddCuration = findViewById(R.id.ll_add_curation)
        llAddRss = findViewById(R.id.ll_add_rss)
        llAddFilter = findViewById(R.id.ll_add_filter)

        btnAddCuration = findViewById(R.id.btn_add_curation)
        btnAddRss = findViewById(R.id.btn_add_rss)
        btnAddFilter = findViewById(R.id.btn_add_filter)
        btnAddCuration.setOnClickListener {
            onAddCurationClicked()
        }
        btnAddRss.setOnClickListener {
            onAddRssClicked()
        }
        btnAddFilter.setOnClickListener {
            onAddFilterClicked()
        }

        fabAddCuration = findViewById(R.id.fab_add_curation)
        fabAddRss = findViewById(R.id.fab_add_rss)
        fabAddFilter = findViewById(R.id.fab_add_filter)
        fabAddCuration.setOnClickListener {
            onAddCurationClicked()
        }
        fabAddRss.setOnClickListener {
            onAddRssClicked()
        }
        fabAddFilter.setOnClickListener {
            onAddFilterClicked()
        }
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_top)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun startFabAnimation() {
        back.visibility = View.VISIBLE

        val animation = AnimationUtils.loadAnimation(this, R.anim.fab_rotation)
        fab.startAnimation(animation)

        llAddCuration.visibility = View.VISIBLE
        btnAddCuration.visibility = View.VISIBLE
        fabAddCuration.show()
        val fadeInCuration = AnimationUtils.loadAnimation(this, R.anim.fab_fadein_curation)
        llAddCuration.startAnimation(fadeInCuration)

        llAddRss.visibility = View.VISIBLE
        btnAddRss.visibility = View.VISIBLE
        fabAddRss.show()
        val fadeInRss = AnimationUtils.loadAnimation(this, R.anim.fab_fadein_rss)
        llAddRss.startAnimation(fadeInRss)

        llAddFilter.visibility = View.VISIBLE
        btnAddFilter.visibility = View.VISIBLE
        fabAddFilter.show()
        val fadeInFilter = AnimationUtils.loadAnimation(this, R.anim.fab_fadein_filter)
        llAddFilter.startAnimation(fadeInFilter)

        onBackPressedCallback.isEnabled = true
    }

    private fun closeAddFab() {
        back.visibility = View.GONE

        val animation = AnimationUtils.loadAnimation(this, R.anim.fab_rotation_back)
        fab.startAnimation(animation)

        val fadeOutCuration = AnimationUtils.loadAnimation(this, R.anim.fab_fadeout_curation)
        fadeOutCuration.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                llAddCuration.visibility = View.GONE
                btnAddCuration.visibility = View.GONE
                fabAddCuration.hide()
            }

        })
        llAddCuration.startAnimation(fadeOutCuration)

        val fadeOutRss = AnimationUtils.loadAnimation(this, R.anim.fab_fadeout_rss)
        fadeOutRss.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                llAddRss.visibility = View.GONE
                btnAddRss.visibility = View.GONE
                fabAddRss.hide()
            }

        })
        llAddRss.startAnimation(fadeOutRss)

        val fadeOutFilter = AnimationUtils.loadAnimation(this, R.anim.fab_fadeout_filter)
        fadeOutFilter.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                llAddFilter.visibility = View.GONE
                btnAddFilter.visibility = View.GONE
                fabAddFilter.hide()
            }

        })
        llAddFilter.startAnimation(fadeOutFilter)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            checkReviewRequestActionCreator.run()
        }
        closeSearchView()
        navigationView.setOnNavigationItemReselectedListener { }
        changeTheme()
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
                if (query == null) return false
                goToArticleSearchResult(query)
                return false
            }
        })

        val color = getThemeColor(R.attr.colorPrimary)
        val searchAutoComplete: TextView = searchView!!
            .findViewById(androidx.appcompat.R.id.search_src_text)
        searchAutoComplete.setTextColor(color)
        searchAutoComplete.setHintTextColor(color)

        // Start tutorial at first time
        if (!BuildConfig.DEBUG && BuildConfig.BUILD_TYPE != "benchmark") {
            Handler(Looper.getMainLooper()).post {
                val view = findViewById<View>(R.id.fab_top)
                MaterialShowcaseView.Builder(this@TopActivity)
                    .setTarget(view)
                    .setContentText(
                        R.string.tutorial_go_to_search_rss_description
                    )
                    .setDismissText(R.string.tutorial_next)
                    .singleUse(SHOWCASE_ID)
                    .setListener(object : IShowcaseListener {
                        override fun onShowcaseDisplayed(materialShowcaseView: MaterialShowcaseView) {

                        }

                        override fun onShowcaseDismissed(materialShowcaseView: MaterialShowcaseView) {
                            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                                goToFeedSearch()
                            }
                        }
                    })
                    .show()
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.setting_top_activity -> goToSetting()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setAlarmManager() {
        // Start auto update alarmmanager
        val manager = AlarmManagerTaskManager(this)
        val intervalSec = helper.autoUpdateIntervalSecond
        manager.setNewAlarm(intervalSec)
    }

    private fun goToFeedSearch() {
        TrackerHelper.sendButtonEvent(getString(R.string.tap_add_rss))
        openFeedSearch.launch(Intent(this@TopActivity, FeedSearchActivity::class.java))
    }

    private fun goToAddCuration() {
        val intent = Intent(applicationContext, AddCurationActivity::class.java)
        startActivity(intent)
        TrackerHelper.sendButtonEvent(getString(R.string.tap_add_curation))
    }

    private fun goToAddFilter() {
        val intent = Intent(applicationContext, RegisterFilterActivity::class.java)
        startActivity(intent)
        TrackerHelper.sendButtonEvent(getString(R.string.tap_add_filter))
    }

    private fun goToSetting() {
        startActivity(Intent(applicationContext, SettingActivity::class.java))
    }

    private fun goToArticleSearchResult(query: String) {
        val intent = Intent(this@TopActivity, ArticleSearchResultActivity::class.java)
        intent.action = Intent.ACTION_SEARCH
        intent.putExtra(SearchManager.QUERY, query)
        startActivity(intent)
    }

    override fun onCurationListClicked(curationId: Int) {
        startActivity(CuratedArticlesListActivity.createIntent(this, curationId))
    }

    override fun startEditCurationActivity(editCurationId: Int) {
        val intent = Intent()
        intent.setClass(this, AddCurationActivity::class.java)
        intent.putExtra(AddCurationActivity.EDIT_CURATION_ID, editCurationId)
        startActivity(intent)
    }

    private fun closeSearchView() {
        if (searchView != null) {
            searchView!!.onActionViewCollapsed()
            searchView!!.setQuery("", false)
        }
    }

    private fun changeTab(position: Int) {
        when (position) {
            PreferenceHelper.LAUNCH_CURATION -> navigationView.selectedItemId =
                R.id.navigation_curation

            PreferenceHelper.LAUNCH_RSS -> navigationView.selectedItemId = R.id.navigation_rss
        }
    }

    private fun showRateDialog() {
        TrackerHelper.sendUiEvent(getString(R.string.show_review_dialog))
        AlertDialog.Builder(this)
            .setTitle(R.string.review_dialog_title)
            .setMessage(R.string.review_dialog_message)
            .setPositiveButton(R.string.review) { _, _ ->
                helper.setReviewed()
                goToGooglePlay()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                TrackerHelper.sendButtonEvent(getString(R.string.cancel_review))
                helper.resetReviewCount()
                lifecycleScope.launchWhenStarted {
                    closeRateDialogActionCreator.run()
                }
            }
            .show()
    }

    private fun goToGooglePlay() {
        TrackerHelper.sendButtonEvent(getString(R.string.tap_go_to_google_play))
        try {
            val uri = Uri.parse("market://details?id=$packageName")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
        }
    }
}
