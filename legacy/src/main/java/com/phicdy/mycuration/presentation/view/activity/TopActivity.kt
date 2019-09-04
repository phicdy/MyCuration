package com.phicdy.mycuration.presentation.view.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.phicdy.mycuration.articlelist.ArticleSearchResultActivity
import com.phicdy.mycuration.articlelist.ArticlesListActivity
import com.phicdy.mycuration.curatedarticlelist.CuratedArticlesListActivity
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.domain.alarm.AlarmManagerTaskManager
import com.phicdy.mycuration.feature.util.changeTheme
import com.phicdy.mycuration.feature.util.getThemeColor
import com.phicdy.mycuration.legacy.BuildConfig
import com.phicdy.mycuration.legacy.R
import com.phicdy.mycuration.presentation.presenter.TopActivityPresenter
import com.phicdy.mycuration.presentation.view.TopActivityView
import com.phicdy.mycuration.presentation.view.fragment.AddCurationFragment
import com.phicdy.mycuration.presentation.view.fragment.CurationListFragment
import com.phicdy.mycuration.presentation.view.fragment.FilterListFragment
import com.phicdy.mycuration.rss.RssListFragment
import com.phicdy.mycuration.tracker.TrackerHelper
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.android.scope.currentScope
import org.koin.core.parameter.parametersOf
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import kotlin.coroutines.CoroutineContext

class TopActivity :
        AppCompatActivity(),
        RssListFragment.OnFeedListFragmentListener,
        CurationListFragment.OnCurationListFragmentListener,
        TopActivityView,
        CoroutineScope {

    companion object {
        private const val SHOWCASE_ID = "tutorialAddRss"
        private const val FRAGMENT_TAG = "FRAGMENT_TAG"
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val presenter: TopActivityPresenter by currentScope.inject { parametersOf(this) }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top)
        presenter.create()
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
    }

    override fun initViewPager() {
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

    override fun initFab() {
        fun onAddCurationClicked() {
            launch(context = coroutineContext) {
                presenter.fabCurationClicked()
            }
        }

        fun onAddRssClicked() {
            presenter.fabRssClicked()
        }

        fun onAddFilterClicked() {
            launch(context = coroutineContext) {
                presenter.fabFilterClicked()
            }
        }

        fab = findViewById(R.id.fab_top)
        fab.setOnClickListener { presenter.fabClicked() }

        back = findViewById(R.id.fl_add_background)
        back.setOnClickListener {
            presenter.addBackgroundClicked()
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
    }

    override fun closeAddFab() {
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
        launch(context = coroutineContext) {
            presenter.resume()
        }
        navigationView.setOnNavigationItemReselectedListener { }
        changeTheme()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
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

        val color = getThemeColor(R.attr.colorPrimary)
        val searchAutoComplete = searchView!!
                .findViewById(androidx.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete
        searchAutoComplete.setTextColor(color)
        searchAutoComplete.setHintTextColor(color)

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
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
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
        startActivity(ArticlesListActivity.createRssIntent(this, feedId))
    }

    override fun onEditRssClicked(rssId: Int, feedTitle: String) {
        val addView = View.inflate(this, R.layout.edit_feed_title, null)
        val editTitleView = addView.findViewById(R.id.editFeedTitle) as EditText
        editTitleView.setText(feedTitle)

        AlertDialog.Builder(this)
                .setTitle(R.string.edit_rss_title)
                .setView(addView)
                .setPositiveButton(R.string.save) { _, _ ->
                    val newTitle = editTitleView.text.toString()
                    launch(context = coroutineContext) {
                        presenter.onEditFeedOkButtonClicked(newTitle, rssId)
                    }
                }.setNegativeButton(R.string.cancel, null).show()
    }

    override fun onDeleteRssClicked(rssId: Int, position: Int) {
        AlertDialog.Builder(this)
                .setTitle(R.string.delete_rss_alert)
                .setPositiveButton(R.string.delete) { _, _ ->
                    launch {
                        presenter.onDeleteOkButtonClicked(rssId, position)
                    }
                }
                .setNegativeButton(R.string.cancel, null).show()
    }

    override suspend fun deleteFeedAtPosition(position: Int) = coroutineScope {
        val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
        if (fragment is RssListFragment) {
            fragment.deleteFeedAtPosition(position)
        }
    }

    override fun showDeleteSuccessToast() {
        Toast.makeText(this, getString(R.string.finish_delete_rss_success), Toast.LENGTH_SHORT).show()
    }

    override fun showDeleteFailToast() {
        Toast.makeText(this, getString(R.string.finish_delete_rss_fail), Toast.LENGTH_SHORT).show()
    }

    override fun onAllUnreadClicked() {
        val intent = Intent(applicationContext, ArticlesListActivity::class.java)
        startActivity(intent)
    }

    override fun onCurationListClicked(curationId: Int) {
        startActivity(CuratedArticlesListActivity.createIntent(this, curationId))
    }

    override fun startEditCurationActivity(editCurationId: Int) {
        val intent = Intent()
        intent.setClass(this, AddCurationActivity::class.java)
        intent.putExtra(AddCurationFragment.EDIT_CURATION_ID, editCurationId)
        startActivity(intent)
    }

    override fun closeSearchView() {
        if (searchView != null) {
            searchView!!.onActionViewCollapsed()
            searchView!!.setQuery("", false)
        }
    }

    private fun changeTab(position: Int) {
        when (position) {
            PreferenceHelper.LAUNCH_CURATION -> navigationView.selectedItemId = R.id.navigation_curation
            PreferenceHelper.LAUNCH_RSS -> navigationView.selectedItemId = R.id.navigation_rss
        }
    }

    override fun showRateDialog() {
        TrackerHelper.sendUiEvent(getString(R.string.show_review_dialog))
        AlertDialog.Builder(this)
                .setTitle(R.string.review_dialog_title)
                .setMessage(R.string.review_dialog_message)
                .setPositiveButton(R.string.review) { _, _ ->
                    presenter.onReviewClicked()
                }
                .setNeutralButton(R.string.request) { _, _ ->
                    TrackerHelper.sendButtonEvent(getString(R.string.tap_request))
                    startActivity(Intent(this, UserRequestActivity::class.java))
                    presenter.onRequestClicked()
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    TrackerHelper.sendButtonEvent(getString(R.string.cancel_review))
                    presenter.onCancelClicked()
                }
                .show()
    }

    override fun goToGooglePlay() {
        TrackerHelper.sendButtonEvent(getString(R.string.tap_go_to_google_play))
        try {
            val uri = Uri.parse("market://details?id=$packageName")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
        }
    }

    override fun showEditFeedTitleEmptyErrorToast() {
        Toast.makeText(this, getString(R.string.empty_title), Toast.LENGTH_SHORT).show()
    }

    override fun showEditFeedFailToast() {
        Toast.makeText(this, getString(R.string.edit_rss_title_error), Toast.LENGTH_SHORT).show()
    }

    override fun showEditFeedSuccessToast() {
        Toast.makeText(this, getString(R.string.edit_rss_title_success), Toast.LENGTH_SHORT).show()
    }

    override fun updateFeedTitle(rssId: Int, newTitle: String) {
        val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
        if (fragment is RssListFragment) {
            fragment.updateFeedTitle(rssId, newTitle)
        }
    }
}

