package com.phicdy.mycuration.domain.task

import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Filter
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

class FilterTask(
    private val articleRepository: ArticleRepository,
    private val filterRepository: FilterRepository
) {

    suspend fun applyFiltering(feedId: Int): Int = coroutineScope {
        val filters = filterRepository.getEnabledFiltersOfFeed(feedId)
        if (filters.size == 0) return@coroutineScope 0
        return@coroutineScope articleRepository.applyFiltersOfRss(filters, feedId)
    }

    suspend fun applyFiltering(articles: List<Article>): List<Article> {
        var feedId = -1
        var filters = listOf<Filter>()
        val result = mutableSetOf<Article>()
        for (article in articles) {
            if (feedId != article.feedId) {
                feedId = article.feedId
                filters = filterRepository.getEnabledFiltersOfFeed(feedId)
            }
            if (filters.isEmpty()) {
                result.add(article)
                continue
            }

            var isFilterd = false
            for (filter in filters) {
                if (filter.keyword.isBlank() && filter.url.isBlank()) {
                    Timber.w("Set filtering conditon, keyword and url don't exist fileter ID =$filter.id")
                    continue
                }

                if ((filter.keyword.isNotBlank() && article.title.contains(filter.keyword)) ||
                    (filter.url.isNotBlank() && article.url.contains(filter.url))
                ) {
                    result.add(article.copy(status = Article.READ))
                    isFilterd = true
                    break
                }
            }
            if (isFilterd.not()) {
                result.add(article)
            }
        }
        return result.toList()
    }
}
