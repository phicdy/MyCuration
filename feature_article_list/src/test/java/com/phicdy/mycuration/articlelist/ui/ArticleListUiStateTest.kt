package com.phicdy.mycuration.articlelist.ui

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.articlelist.ArticleListUiBinding
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.FavoritableArticle
import com.phicdy.mycuration.entity.Feed
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ArticleListUiStateTest {

    private fun article(
        id: Int = 1,
        status: String = Article.UNREAD,
        point: String = "10",
        feedTitle: String = "feed",
        feedIconPath: String = "/path/to/icon",
        isFavorite: Boolean = false,
    ) = FavoritableArticle(
        id = id,
        title = "title$id",
        url = "https://example.com/$id",
        status = status,
        point = point,
        postedDate = 1000L,
        feedId = 2,
        feedTitle = feedTitle,
        feedIconPath = feedIconPath,
        isFavorite = isFavorite
    )

    @Test
    fun `article is mapped to content row`() {
        val row = article(isFavorite = true).toArticleRowUi()
        assertThat(row).isEqualTo(
            ArticleRowUi.Content(
                id = 1,
                title = "title1",
                url = "https://example.com/1",
                isRead = false,
                isFavorite = true,
                postedDate = 1000L,
                point = "10",
                feedId = 2,
                feedTitle = "feed",
                feedIconPath = "/path/to/icon",
            )
        )
        assertThat(row.key).isEqualTo("article-1")
    }

    @Test
    fun `read article is mapped to read row`() {
        assertThat(article(status = Article.READ).toArticleRowUi().isRead).isTrue()
    }

    @Test
    fun `default hatena point is mapped to null`() {
        assertThat(article(point = Article.DEDAULT_HATENA_POINT).toArticleRowUi().point).isNull()
    }

    @Test
    fun `empty feed title is mapped to null`() {
        assertThat(article(feedTitle = "").toArticleRowUi().feedTitle).isNull()
    }

    @Test
    fun `default or blank icon path is mapped to null`() {
        assertThat(article(feedIconPath = Feed.DEDAULT_ICON_PATH).toArticleRowUi().feedIconPath).isNull()
        assertThat(article(feedIconPath = " ").toArticleRowUi().feedIconPath).isNull()
    }

    @Test
    fun `advertisements get unique keys by position`() {
        val rows = listOf(
            ArticleItem.Content(article(id = 1)),
            ArticleItem.Advertisement,
            ArticleItem.Content(article(id = 2)),
            ArticleItem.Advertisement,
        ).toArticleRowsUi()
        assertThat(rows.map { it.key }).containsExactly("article-1", "ad-1", "article-2", "ad-3")
        assertThat(rows[1]).isEqualTo(ArticleRowUi.Advertisement(1))
    }

    @Test
    fun `init binding is mapped to loading`() {
        assertThat(ArticleListUiBinding.Init.toUiState()).isEqualTo(ArticleListUiState.Loading)
    }

    @Test
    fun `empty loaded binding is mapped to no article`() {
        assertThat(ArticleListUiBinding.Loaded(emptyList()).toUiState())
            .isEqualTo(ArticleListUiState.Empty(ArticleListUiState.EmptyReason.NO_ARTICLE))
    }

    @Test
    fun `empty searched binding is mapped to no search result`() {
        assertThat(ArticleListUiBinding.Searched(emptyList()).toUiState())
            .isEqualTo(ArticleListUiState.Empty(ArticleListUiState.EmptyReason.NO_SEARCH_RESULT))
    }

    @Test
    fun `non empty binding is mapped to loaded rows`() {
        val state = ArticleListUiBinding.Searched(listOf(ArticleItem.Content(article()))).toUiState()
        assertThat(state).isEqualTo(ArticleListUiState.Loaded(listOf(article().toArticleRowUi())))
    }
}
