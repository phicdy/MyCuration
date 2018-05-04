package com.phicdy.mycuration.presentation.presenter

import android.view.KeyEvent
import android.view.MenuItem

import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.presentation.view.TopActivityView

class TopActivityPresenter(private val launchTab: Int, private val view: TopActivityView,
                           private val dbAdapter: DatabaseAdapter) : Presenter {

    override fun create() {
        view.initViewPager()
        view.initFab()
        view.initToolbar()
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
        view.startFabAnimation()
    }

    fun fabCurationClicked() {
        view.closeAddFab()
        if (dbAdapter.numOfFeeds == 0) {
            view.goToFeedSearch()
            return
        }
        view.goToAddCuration()
    }

    fun fabRssClicked() {
        view.closeAddFab()
        view.goToFeedSearch()
    }

    fun fabFilterClicked() {
        view.closeAddFab()
        if (dbAdapter.numOfFeeds == 0) {
            view.goToFeedSearch()
            return
        }
        view.goToAddFilter()
    }

    fun addBackgroundClicked() {
        view.closeAddFab()
    }

    private fun settingMenuClicked() {
        view.goToSetting()
    }

    fun optionItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.setting_top_activity -> settingMenuClicked()
        }
    }

    fun onKeyDown(keyCode: Int, isShowAddFabs: Boolean): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && isShowAddFabs) {
            view.closeAddFab()
            return true
        }
        return false
    }

    fun queryTextSubmit(query: String?) {
        if (query == null) return
        view.goToArticleSearchResult(query)
    }
}
