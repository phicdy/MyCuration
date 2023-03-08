package com.phicdy.mycuration.articlelist.action

import com.nhaarman.mockitokotlin2.mock
import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.FavoritableArticle
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReadArticlePositionActionCreatorTest {

    @Test
    fun `when read unread article then article status becomes TOREAD`() {
        runBlocking {
            val article = FavoritableArticle(
                    id = 1,
                    title = "aaa",
                    point = "1",
                    status = Article.UNREAD,
                    postedDate = 0,
                    feedId = 0,
                    feedTitle = "bbb",
                    feedIconPath = "ccc",
                    url = "ddd",
                    isFavorite = false
            )
            ReadArticleActionCreator(
                    dispatcher = mock(),
                    articleRepository = mock(),
                    rssRepository = mock()
            ).run(
                    position = 0,
                    items = listOf(ArticleItem.Content(article))
            )
            assertThat(article.status).isEqualTo(Article.READ)
        }
    }

    @Test
    fun `when read to read article then article status is still TOREAD`() {
        runBlocking {
            val article = FavoritableArticle(
                    id = 1,
                    title = "aaa",
                    point = "1",
                    status = Article.READ,
                    postedDate = 0,
                    feedId = 0,
                    feedTitle = "bbb",
                    feedIconPath = "ccc",
                    url = "ddd",
                    isFavorite = false
            )
            ReadArticleActionCreator(
                    dispatcher = mock(),
                    articleRepository = mock(),
                    rssRepository = mock()
            ).run(
                    position = 0,
                    items = listOf(ArticleItem.Content(article))
            )
            assertThat(article.status).isEqualTo(Article.READ)
        }
    }

    @Test
    fun `when read read article then article status is still TOREAD`() {
        runBlocking {
            val article = FavoritableArticle(
                    id = 1,
                    title = "aaa",
                    point = "1",
                    status = Article.READ,
                    postedDate = 0,
                    feedId = 0,
                    feedTitle = "bbb",
                    feedIconPath = "ccc",
                    url = "ddd",
                    isFavorite = false
            )
            ReadArticleActionCreator(
                    dispatcher = mock(),
                    articleRepository = mock(),
                    rssRepository = mock()
            ).run(
                    position = 0,
                    items = listOf(ArticleItem.Content(article))
            )
            assertThat(article.status).isEqualTo(Article.READ)
        }
    }
}