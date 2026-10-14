package com.phicdy.mycuration.articlelist

import com.phicdy.mycuration.entity.FavoritableArticle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

class ArticleListUiBindingTest {

    @Test
    fun `toArticleItems of Init is empty`() {
        assertThat(ArticleListUiBinding.Init.toArticleItems()).isEmpty()
    }

    @Test
    fun `toArticleItems of Loaded is the loaded list`() {
        val list = listOf(ArticleItem.Advertisement, ArticleItem.Content(mock<FavoritableArticle>()))
        assertThat(ArticleListUiBinding.Loaded(list).toArticleItems()).isSameAs(list)
    }

    @Test
    fun `toArticleItems of Searched is the searched list`() {
        val list = listOf(ArticleItem.Content(mock<FavoritableArticle>()))
        assertThat(ArticleListUiBinding.Searched(list).toArticleItems()).isSameAs(list)
    }
}
