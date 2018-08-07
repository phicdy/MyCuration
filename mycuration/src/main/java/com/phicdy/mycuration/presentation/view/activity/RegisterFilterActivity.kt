package com.phicdy.mycuration.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.presentation.presenter.RegisterFilterPresenter
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.tracker.GATrackerHelper
import com.phicdy.mycuration.presentation.view.fragment.FilterListFragment
import com.phicdy.mycuration.presentation.view.RegisterFilterView


class RegisterFilterActivity : AppCompatActivity(), RegisterFilterView {

    private lateinit var presenter: RegisterFilterPresenter

    private lateinit var etTitle: EditText
    private lateinit var etKeyword: EditText
    private lateinit var etFilterUrl: EditText
    private lateinit var tvTargetRss: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_filter)

        initView()
        val dbAdapter = DatabaseAdapter.getInstance()
        val editFilterId = intent.getIntExtra(FilterListFragment.KEY_EDIT_FILTER_ID, NEW_FILTER_ID)
        presenter = RegisterFilterPresenter(dbAdapter, editFilterId)
        presenter.setView(this)
    }

    private fun initView() {
        setTitle(R.string.add_filter)

        etKeyword = findViewById(R.id.filterKeyword) as EditText
        etFilterUrl = findViewById(R.id.filterUrl) as EditText
        etTitle = findViewById(R.id.filterTitle) as EditText

        //Set spinner
        tvTargetRss = findViewById(R.id.tv_target_rss) as TextView
        tvTargetRss.setOnClickListener {
            val intent = Intent(this@RegisterFilterActivity, SelectFilterTargetRssActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelableArrayList(SelectFilterTargetRssActivity.TARGET_RSS, presenter.selectedFeedList())
            intent.putExtras(bundle)
            startActivityForResult(intent, TARGET_FEED_SELECT_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == TARGET_FEED_SELECT_REQUEST) {
            val bundle = data.extras
            val list = bundle.getParcelableArrayList<Feed>(KEY_SELECTED_FEED)
            presenter.setSelectedFeedList(list)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_register_filter, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        presenter.optionItemClicked(item)
        return super.onOptionsItemSelected(item)
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
        GATrackerHelper.sendButtonEvent(getString(R.string.add_new_filter_no_title))
    }

    override fun handleEmptyCondition() {
        Toast.makeText(this@RegisterFilterActivity, R.string.both_keyword_and_url_empty_error, Toast.LENGTH_SHORT).show()
        GATrackerHelper.sendButtonEvent(getString(R.string.add_new_filter_no_keyword_url))
    }

    override fun handlePercentOnly() {
        Toast.makeText(this@RegisterFilterActivity, R.string.percent_only_error, Toast.LENGTH_SHORT).show()
    }

    override fun showSaveSuccessToast() {
        Toast.makeText(applicationContext, getString(R.string.filter_saved), Toast.LENGTH_SHORT).show()
    }

    override fun showSaveErrorToast() {
        Toast.makeText(applicationContext, getString(R.string.filter_saved_error), Toast.LENGTH_SHORT).show()
    }

    override fun trackEdit() {
        GATrackerHelper.sendButtonEvent(getString(R.string.update_filter))
    }

    override fun trackRegister() {
        GATrackerHelper.sendButtonEvent(getString(R.string.add_new_filter))
    }

    companion object {

        const val KEY_SELECTED_FEED = "keySelectedFeed"
        private const val NEW_FILTER_ID = -1
        private const val TARGET_FEED_SELECT_REQUEST = 1000
    }
}
