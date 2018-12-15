package com.phicdy.mycuration.domain.filter

import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import kotlinx.coroutines.coroutineScope

class FilterTask(private val articleRepository: ArticleRepository,
                 private val filterRepository: FilterRepository) {

    suspend fun applyFiltering(feedId: Int): Int = coroutineScope {
        val filters = filterRepository.getEnabledFiltersOfFeed(feedId)
        if (filters.size == 0) return@coroutineScope 0
        return@coroutineScope articleRepository.applyFiltersOfRss(filters, feedId)
    }
}
