package com.phicdy.mycuration.top

import android.view.KeyEvent
import android.view.MenuItem
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class TopActivityPresenter @Inject constructor(
    private val view: TopActivityView,
    private val articleRepository: ArticleRepository,
    private val rssRepository: RssRepository,
    private val helper: PreferenceHelper
) {

    fun create() {
        view.initViewPager()
        view.initFab()
        view.initToolbar()
        view.setAlarmManager()
    }

    suspend fun resume() = coroutineScope {
        if (!helper.isReviewed() && helper.getReviewCount() - 1 <= 0) {
            view.showRateDialog()
            helper.resetReviewCount()
        } else {
            if (helper.getReviewCount() <= 0) {
                helper.resetReviewCount()
            } else {
                helper.decreaseReviewCount()
            }
        }
        view.closeSearchView()
    }

    fun fabClicked() {
        view.startFabAnimation()
    }

    fun fabCurationClicked() {
        view.closeAddFab()
        val num = rssRepository.getNumOfRss()
        if (num == 0L) {
            view.goToFeedSearch()
            return
        }
        view.goToAddCuration()
    }

    fun fabRssClicked() {
        view.closeAddFab()
        view.goToFeedSearch()
    }

    suspend fun fabFilterClicked() = coroutineScope {
        view.closeAddFab()
        val num = rssRepository.getNumOfRss()
        if (num == 0L) {
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

    fun onReviewClicked() {
        helper.setReviewed()
        view.goToGooglePlay()
    }

    fun onRequestClicked() {
        helper.setReviewed()
    }

    fun onCancelClicked() {
        helper.resetReviewCount()
    }

    suspend fun onEditFeedOkButtonClicked(newTitle: String, rssId: Int) = coroutineScope {
        if (newTitle.isBlank()) {
            view.showEditFeedTitleEmptyErrorToast()
        } else {
            val numOfUpdate = rssRepository.saveNewTitle(rssId, newTitle)
            if (numOfUpdate == 1) {
                view.showEditFeedSuccessToast()
                view.updateFeedTitle(rssId, newTitle)
            } else {
                view.showEditFeedFailToast()
            }
        }
    }

    suspend fun onDeleteOkButtonClicked(rssId: Int) {
        if (rssRepository.deleteRss(rssId)) {
            view.removeRss(rssId)
            view.showDeleteSuccessToast()
        } else {
            view.showDeleteFailToast()
        }
    }
}
