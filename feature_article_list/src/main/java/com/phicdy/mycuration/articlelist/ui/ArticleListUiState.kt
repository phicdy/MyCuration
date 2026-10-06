package com.phicdy.mycuration.articlelist.ui

import androidx.compose.runtime.Immutable
import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.articlelist.ArticleListUiBinding
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.FavoritableArticle
import com.phicdy.mycuration.entity.Feed

/**
 * Immutable snapshot of the article list for rendering.
 *
 * Action creators still mutate [FavoritableArticle.status] in place, so this state must be
 * re-created from the raw [ArticleItem] list after every interaction that changes an item.
 */
@Immutable
sealed interface ArticleListUiState {
    data object Loading : ArticleListUiState

    data class Empty(val reason: EmptyReason) : ArticleListUiState

    data class Loaded(val rows: List<ArticleRowUi>) : ArticleListUiState

    enum class EmptyReason {
        /** Corresponds to R.string.no_article */
        NO_ARTICLE,

        /** Corresponds to R.string.no_search_result */
        NO_SEARCH_RESULT,
    }
}

/**
 * Immutable model of one row in the article list.
 */
@Immutable
sealed interface ArticleRowUi {
    /** Stable and unique key, e.g. for LazyColumn items. */
    val key: String

    data class Content(
        val id: Int,
        val title: String,
        val url: String,
        val isRead: Boolean,
        val isFavorite: Boolean,
        /** Posted date in epoch millis. */
        val postedDate: Long,
        /** Hatena point, or null when it has not been fetched yet (R.string.not_get_hatena_point). */
        val point: String?,
        val feedId: Int,
        /** Feed title, or null when feed title and icon should be hidden. */
        val feedTitle: String?,
        /** Feed icon path to load, or null when the default RSS icon should be shown. */
        val feedIconPath: String?,
    ) : ArticleRowUi {
        override val key: String get() = "article-$id"
    }

    /**
     * Advertisement row. [ArticleItem.Advertisement] is a singleton that can appear multiple
     * times, so the position in the list is used to keep the key unique.
     */
    data class Advertisement(val position: Int) : ArticleRowUi {
        override val key: String get() = "ad-$position"
    }
}

fun FavoritableArticle.toArticleRowUi(): ArticleRowUi.Content = ArticleRowUi.Content(
    id = id,
    title = title,
    url = url,
    isRead = status == Article.READ,
    isFavorite = isFavorite,
    postedDate = postedDate,
    point = if (point == Article.DEDAULT_HATENA_POINT) null else point,
    feedId = feedId,
    feedTitle = feedTitle.ifEmpty { null },
    feedIconPath = if (feedIconPath.isNotBlank() && feedIconPath != Feed.DEDAULT_ICON_PATH) {
        feedIconPath
    } else {
        null
    },
)

fun List<ArticleItem>.toArticleRowsUi(): List<ArticleRowUi> = mapIndexed { index, item ->
    when (item) {
        is ArticleItem.Content -> item.value.toArticleRowUi()
        is ArticleItem.Advertisement -> ArticleRowUi.Advertisement(index)
    }
}

fun ArticleListUiBinding.toUiState(): ArticleListUiState = when (this) {
    ArticleListUiBinding.Init -> ArticleListUiState.Loading
    is ArticleListUiBinding.Loaded -> list.toUiState(ArticleListUiState.EmptyReason.NO_ARTICLE)
    is ArticleListUiBinding.Searched -> list.toUiState(ArticleListUiState.EmptyReason.NO_SEARCH_RESULT)
}

private fun List<ArticleItem>.toUiState(emptyReason: ArticleListUiState.EmptyReason): ArticleListUiState =
    if (isEmpty()) {
        ArticleListUiState.Empty(emptyReason)
    } else {
        ArticleListUiState.Loaded(toArticleRowsUi())
    }
