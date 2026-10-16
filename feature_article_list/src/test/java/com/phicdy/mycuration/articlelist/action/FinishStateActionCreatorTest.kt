package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.FavoritableArticle
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class FinishStateActionCreatorTest {

    private fun article(id: Int, status: String): ArticleItem = ArticleItem.Content(
        FavoritableArticle(
            id = id,
            title = "title$id",
            url = "https://example.com/$id",
            status = status,
            point = "1",
            postedDate = 0,
            feedId = 1,
            feedTitle = "feed",
            feedIconPath = "icon",
            isFavorite = false
        )
    )

    private val allRead: List<ArticleItem> = listOf(
        ArticleItem.Advertisement,
        article(1, Article.READ),
        article(2, Article.READ),
    )

    private fun finishStateActionCreator(dispatcher: Dispatcher, allReadBack: Boolean): FinishStateActionCreator {
        val preferenceHelper = mock<PreferenceHelper> {
            on { this.allReadBack } doReturn allReadBack
        }
        return FinishStateActionCreator(dispatcher = dispatcher, preferenceHelper = preferenceHelper)
    }

    @Test
    fun `when all articles are read in one pane then finish`(): Unit = runBlocking {
        val dispatcher = mock<Dispatcher>()
        finishStateActionCreator(dispatcher, allReadBack = true).run(allRead, isTwoPane = false)
        argumentCaptor<FinishAction> {
            verify(dispatcher).dispatch(capture())
        }
    }

    @Test
    fun `when all articles are read in two panes then do not finish`(): Unit = runBlocking {
        val dispatcher = mock<Dispatcher>()
        finishStateActionCreator(dispatcher, allReadBack = true).run(allRead, isTwoPane = true)
        verify(dispatcher, never()).dispatch(any<FinishAction>())
    }

    @Test
    fun `when an article is unread then do not finish`(): Unit = runBlocking {
        val dispatcher = mock<Dispatcher>()
        val items = allRead + article(3, Article.UNREAD)
        finishStateActionCreator(dispatcher, allReadBack = true).run(items, isTwoPane = false)
        verify(dispatcher, never()).dispatch(any<FinishAction>())
    }

    @Test
    fun `when the setting does not go back then do not finish`(): Unit = runBlocking {
        val dispatcher = mock<Dispatcher>()
        finishStateActionCreator(dispatcher, allReadBack = false).run(allRead, isTwoPane = false)
        verify(dispatcher, never()).dispatch(any<FinishAction>())
    }

    @Test
    fun `run without two-pane flag behaves as one pane`(): Unit = runBlocking {
        val dispatcher = mock<Dispatcher>()
        finishStateActionCreator(dispatcher, allReadBack = true).run(allRead)
        argumentCaptor<FinishAction> {
            verify(dispatcher).dispatch(capture())
        }
    }
}
