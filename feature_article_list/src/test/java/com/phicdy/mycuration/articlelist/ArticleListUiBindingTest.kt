package com.phicdy.mycuration.articlelist

import com.phicdy.mycuration.entity.FavoritableArticle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

class ArticleListUiBindingTest {

    @Test
    fun `articles of Init is empty`() {
        assertThat(ArticleListUiBinding.Init.articles()).isEmpty()
    }

    @Test
    fun `articles of Loaded is the loaded list`() {
        val list = listOf(ArticleItem.Advertisement, ArticleItem.Content(mock<FavoritableArticle>()))
        assertThat(ArticleListUiBinding.Loaded(list).articles()).isSameAs(list)
    }

    @Test
    fun `articles of Searched is the searched list`() {
        val list = listOf(ArticleItem.Content(mock<FavoritableArticle>()))
        assertThat(ArticleListUiBinding.Searched(list).articles()).isSameAs(list)
    }
}
