package com.phicdy.mycuration.presentation.presenter

import android.view.KeyEvent
import android.view.MenuItem
import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.presentation.view.TopActivityView
import kotlinx.coroutines.coroutineScope

class TopActivityPresenter(private val view: TopActivityView,
                           private val articleRepository: ArticleRepository,
                           private val rssRepository: RssRepository
) {

    fun create() {
        view.initViewPager()
        view.initFab()
        view.initToolbar()
        view.setAlarmManager()
    }

    suspend fun resume() = coroutineScope {
        articleRepository.saveAllStatusToReadFromToRead()
        view.closeSearchView()
    }

    fun fabClicked() {
        view.startFabAnimation()
    }

    suspend fun fabCurationClicked() = coroutineScope {
        view.closeAddFab()
        if (rssRepository.getNumOfRss() == 0) {
            view.goToFeedSearch()
            return@coroutineScope
        }
        view.goToAddCuration()
    }

    fun fabRssClicked() {
        view.closeAddFab()
        view.goToFeedSearch()
    }

    suspend fun fabFilterClicked() = coroutineScope {
        view.closeAddFab()
        if (rssRepository.getNumOfRss() == 0) {
            view.goToFeedSearch()
            return@coroutineScope
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
