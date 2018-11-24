package com.phicdy.mycuration.domain.filter

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.repository.ArticleRepository
import kotlinx.coroutines.coroutineScope

class FilterTask(private val articleRepository: ArticleRepository) {

    suspend fun applyFiltering(feedId: Int): Int = coroutineScope {
        val dbAdapter = DatabaseAdapter.getInstance()
        val filters = dbAdapter.getEnabledFiltersOfFeed(feedId)
        if (filters.size == 0) return@coroutineScope 0
        return@coroutineScope articleRepository.applyFiltersOfRss(filters, feedId)
    }
}
