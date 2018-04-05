package com.phicdy.mycuration.presentation.presenter

import android.view.MenuItem

import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.presentation.view.TopActivityView

class TopActivityPresenter(private val launchTab: Int, private val view: TopActivityView,
                           private val dbAdapter: DatabaseAdapter) : Presenter {

    companion object {
        private const val POSITION_CURATION_FRAGMENT = 0
        private const val POSITION_FEED_FRAGMENT = 1
        private const val POSITION_FILTER_FRAGMENT = 2
    }

    override fun create() {
        view.initViewPager()
        view.initFab()
        view.setAlarmManager()
        view.changeTab(launchTab)
    }

    override fun resume() {
        Thread(Runnable { dbAdapter.saveAllStatusToReadFromToRead() }).start()

        view.closeSearchView()
    }

    override fun pause() {

    }

    fun fabClicked() {
        when (view.currentTabPosition()) {
            POSITION_CURATION_FRAGMENT -> {
                if (dbAdapter.numOfFeeds == 0) {
                    view.goToFeedSearch()
                    return
                }
                view.goToAddCuration()
            }
            POSITION_FEED_FRAGMENT -> view.goToFeedSearch()
            POSITION_FILTER_FRAGMENT -> {
                if (dbAdapter.numOfFeeds == 0) {
                    view.goToFeedSearch()
                    return
                }
                view.goToAddFilter()
            }
        }
    }

    private fun settingMenuClicked() {
        view.goToSetting()
    }

    fun optionItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.setting_top_activity -> settingMenuClicked()
        }
    }
}
