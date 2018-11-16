package com.phicdy.mycuration.domain.filter

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.repository.ArticleRepository
import kotlinx.coroutines.coroutineScope

class FilterTask(private val articleRepository: ArticleRepository) {

    suspend fun applyFiltering(feedId: Int) = coroutineScope {
        val dbAdapter = DatabaseAdapter.getInstance()
        dbAdapter.getEnabledFiltersOfFeed(feedId).let {
            if (it.size == 0) return@coroutineScope
            articleRepository.applyFiltersOfRss(it, feedId)
        }
    }
}
