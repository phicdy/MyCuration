package com.phicdy.feature_register_filter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.feature.util.changeTheme
import com.phicdy.mycuration.tracker.TrackerHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class RegisterFilterActivity : AppCompatActivity(), RegisterFilterView {

    @Inject
    lateinit var presenter: RegisterFilterPresenter

    private lateinit var etTitle: TextInputEditText
    private lateinit var etKeyword: TextInputEditText
    private lateinit var etFilterUrl: TextInputEditText
    private lateinit var tvTargetRss: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_filter)

        initView()
        lifecycleScope.launch {
            presenter.create()
        }
    }

    private fun initView() {
        etKeyword = findViewById(R.id.filterKeyword)
        etFilterUrl = findViewById(R.id.filterUrl)
        etTitle = findViewById(R.id.filterTitle)

        //Set spinner
        tvTargetRss = findViewById(R.id.tv_target_rss)
        tvTargetRss.setOnClickListener {
            val intent = Intent(this@RegisterFilterActivity, SelectFilterTargetRssActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelableArrayList(SelectFilterTargetRssActivity.TARGET_RSS, presenter.selectedFeedList())
            intent.putExtras(bundle)
            startActivityForResult(intent, TARGET_FEED_SELECT_REQUEST)
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar_register_filter)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.add_filter)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == TARGET_FEED_SELECT_REQUEST) {
            data?.extras?.let {
                val list = it.getParcelableArrayList<Feed>(KEY_SELECTED_FEED) ?: arrayListOf()
                presenter.setSelectedFeedList(list)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_register_filter, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // For arrow button on toolbar
            android.R.id.home -> finish()
            else -> lifecycleScope.launchWhenStarted { presenter.optionItemClicked(item) }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        changeTheme()
    }

    override fun filterKeyword(): String {
        return etKeyword.text.toString()
    }

    override fun filterUrl(): String {
        return etFilterUrl.text.toString()
    }

    override fun filterTitle(): String {
        return etTitle.text.toString()
    }

    override fun setFilterTitle(title: String) {
        etTitle.setText(title)
    }

    override fun setFilterTargetRss(rss: String) {
        tvTargetRss.text = rss
    }

    override fun setMultipleFilterTargetRss() {
        tvTargetRss.setText(R.string.multiple_target_rss)
    }

    override fun resetFilterTargetRss() {
        tvTargetRss.setText(R.string.target_rss)
    }

    override fun setFilterUrl(url: String) {
        etFilterUrl.setText(url)
    }

    override fun setFilterKeyword(keyword: String) {
        etKeyword.setText(keyword)
    }

    override fun handleEmptyTitle() {
        Toast.makeText(this@RegisterFilterActivity, R.string.title_empty_error, Toast.LENGTH_SHORT).show()
        TrackerHelper.sendButtonEvent(getString(R.string.add_new_filter_no_title))
    }

    override fun handleEmptyCondition() {
        Toast.makeText(this@RegisterFilterActivity, R.string.both_keyword_and_url_empty_error, Toast.LENGTH_SHORT).show()
        TrackerHelper.sendButtonEvent(getString(R.string.add_new_filter_no_keyword_url))
    }

    override fun handlePercentOnly() {
        Toast.makeText(this@RegisterFilterActivity, R.string.percent_only_error, Toast.LENGTH_SHORT).show()
    }

    override fun showSaveSuccessToast() {
        Toast.makeText(applicationContext, getString(R.string.filter_saved), Toast.LENGTH_SHORT).show()
    }

    override fun showSaveErrorToast() {
        Toast.makeText(
            applicationContext,
            getString(R.string.filter_saved_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun trackEdit() {
        TrackerHelper.sendButtonEvent(getString(R.string.update_filter))
    }

    override fun trackRegister() {
        TrackerHelper.sendButtonEvent(getString(R.string.add_new_filter))
    }

    override fun handleEmptyFeed() {
        Toast.makeText(
            applicationContext,
            getString(R.string.target_rss_empty_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        const val KEY_EDIT_FILTER_ID = "editFilterId"
        const val KEY_SELECTED_FEED = "keySelectedFeed"
        private const val NEW_FILTER_ID = -1
        private const val TARGET_FEED_SELECT_REQUEST = 1000
    }

    @Module
    @InstallIn(ActivityComponent::class)
    object RegisterFilterModule {
        @ActivityScoped
        @Provides
        fun provideEditFilterId(@ActivityContext activity: Context): Int =
            (activity as AppCompatActivity).intent.getIntExtra(KEY_EDIT_FILTER_ID, NEW_FILTER_ID)

        @ActivityScoped
        @Provides
        fun provideRegisterFilterView(@ActivityContext activity: Context): RegisterFilterView =
                activity as RegisterFilterView
    }
}
