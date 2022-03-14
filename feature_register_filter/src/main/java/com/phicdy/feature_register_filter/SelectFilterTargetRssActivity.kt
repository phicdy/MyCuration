package com.phicdy.feature_register_filter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.feature.util.changeTheme
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@AndroidEntryPoint
class SelectFilterTargetRssActivity : AppCompatActivity(), SelectTargetRssView {

    companion object {
        const val TARGET_RSS = "targetRss"
    }

    @Inject
    lateinit var presenter: SelectFilterTargetRssPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_filter_target_rss)
        title = getString(R.string.title_select_filter_rss)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        val rssFragment = fragment as? SelectFilterTargetRssFragment // maybe Glide's fragment
        val selectedList = intent.getParcelableArrayListExtra<Feed>(TARGET_RSS) ?: throw IllegalArgumentException("RSS is not selected")
        rssFragment?.updateSelected(selectedList.toMutableList())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_select_filter_rss, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // For arrow button on toolbar
            android.R.id.home -> finish()
            else -> presenter.optionItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        changeTheme()
    }

    override fun finishSelect() {
        val data = Intent()
        val bundle = Bundle()
        val manager = supportFragmentManager
        val fragment = manager.findFragmentById(R.id.f_select_target) as SelectFilterTargetRssFragment
        val rssList = arrayListOf<Feed>()
        fragment.list().forEach { rssList.add(it) }
        bundle.putParcelableArrayList(RegisterFilterActivity.KEY_SELECTED_FEED, rssList)
        data.putExtras(bundle)
        setResult(RESULT_OK, data)
        finish()
    }

    @Module
    @InstallIn(ActivityComponent::class)
    object SelectFilterTargetRssModule {
        @ActivityScoped
        @Provides
        fun provideSelectTargetRssView(@ActivityContext activity: Context): SelectTargetRssView = activity as SelectTargetRssView
    }
}
