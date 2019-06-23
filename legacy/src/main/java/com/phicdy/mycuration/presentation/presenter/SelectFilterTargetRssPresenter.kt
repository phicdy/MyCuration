package com.phicdy.mycuration.presentation.presenter

import android.view.MenuItem
import com.phicdy.mycuration.legacy.R

import com.phicdy.mycuration.presentation.view.SelectTargetRssView

class SelectFilterTargetRssPresenter(private val view: SelectTargetRssView) : Presenter {

    override fun create() {}

    override fun resume() {}

    override fun pause() {}

    fun optionItemSelected(item: MenuItem) {
        when (item.itemId) {
            R.id.done_select_target_rss -> view.finishSelect()
        }
    }
}
