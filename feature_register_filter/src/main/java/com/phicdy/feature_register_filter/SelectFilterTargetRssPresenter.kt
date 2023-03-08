package com.phicdy.feature_register_filter

import android.view.MenuItem
import javax.inject.Inject

class SelectFilterTargetRssPresenter @Inject constructor(private val view: SelectTargetRssView) {

    fun optionItemSelected(item: MenuItem) {
        when (item.itemId) {
            R.id.done_select_target_rss -> view.finishSelect()
        }
    }
}
