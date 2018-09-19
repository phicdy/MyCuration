package com.phicdy.mycuration.domain.filter

import com.phicdy.mycuration.data.db.DatabaseAdapter

class FilterTask {

    fun applyFiltering(feedId: Int) {
        val dbAdapter = DatabaseAdapter.getInstance()
        dbAdapter.getEnabledFiltersOfFeed(feedId).let {
            if (it.size == 0) return
            dbAdapter.applyFiltersOfFeed(it, feedId)
        }
    }
}
