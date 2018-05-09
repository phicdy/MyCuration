package com.phicdy.mycuration.presentation.view.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.presentation.presenter.SelectFilterTargetRssPresenter
import com.phicdy.mycuration.presentation.view.SelectTargetRssView
import com.phicdy.mycuration.presentation.view.fragment.SelectFilterTargetRssFragment

class SelectFilterTargetRssActivity : AppCompatActivity(), SelectTargetRssView {

    companion object {
        const val TARGET_RSS = "targetRss"
    }

    private lateinit var presenter: SelectFilterTargetRssPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_filter_target_rss)
        title = getString(R.string.title_select_filter_rss)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        presenter = SelectFilterTargetRssPresenter()
        presenter.setView(this)
        presenter.create()
    }

    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)
        val rssFragment = fragment as SelectFilterTargetRssFragment
        val selectedList = intent.getParcelableArrayListExtra<Feed>(TARGET_RSS)
        rssFragment.updateSelected(selectedList)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_select_filter_rss, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        presenter.optionItemSelected(item)
        return super.onOptionsItemSelected(item)
    }

    override fun finishSelect() {
        val data = Intent()
        val bundle = Bundle()
        val manager = supportFragmentManager
        val fragment = manager.findFragmentById(R.id.f_select_target) as SelectFilterTargetRssFragment
        bundle.putParcelableArrayList(RegisterFilterActivity.KEY_SELECTED_FEED, fragment.list())
        data.putExtras(bundle)
        setResult(RESULT_OK, data)
        finish()
    }
}
