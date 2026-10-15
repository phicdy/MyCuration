package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.articlelist.SelectedArticle
import com.phicdy.mycuration.articlelist.store.OpenExternalWebBrowserStateStore
import com.phicdy.mycuration.articlelist.store.OpenInternalWebBrowserStateStore
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.entity.FavoritableArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@ExperimentalCoroutinesApi
class OpenUrlActionCreatorTest {

    @Test
    fun `when internal option is enabled then open internal browser`() {
        val dispatcher = mock<Dispatcher>()
        runBlocking {
            val preferenceHelper = mock<PreferenceHelper> {
                on { isOpenInternal } doReturn true
            }
            val store = mock<OpenInternalWebBrowserStateStore> {
                on { coroutineContext } doReturn Dispatchers.Unconfined
            }
            dispatcher.register(store)
            val article = mock<FavoritableArticle> {
                on { copy(feedTitle = "") } doReturn mock()
                on { url } doReturn "aaa"
            }
            OpenUrlActionCreator(
                    dispatcher = dispatcher,
                    preferenceHelper = preferenceHelper
            ).run(item = ArticleItem.Content(article))
        }
        runBlocking {
            argumentCaptor<OpenInternalBrowserAction> {
                verify(dispatcher).dispatch(capture())
            }
        }
    }

    @Test
    fun `when external option is enabled then open external browser`() {
        val dispatcher = mock<Dispatcher>()
        runBlocking {
            val preferenceHelper = mock<PreferenceHelper> {
                on { isOpenInternal } doReturn false
            }
            val store = mock<OpenExternalWebBrowserStateStore> {
                on { coroutineContext } doReturn Dispatchers.Unconfined
            }
            dispatcher.register(store)
            val article = mock<FavoritableArticle> {
                on { copy(feedTitle = "") } doReturn mock()
                on { url } doReturn "aaa"
            }
            OpenUrlActionCreator(
                    dispatcher = dispatcher,
                    preferenceHelper = preferenceHelper
            ).run(item = ArticleItem.Content(article))
        }
        runBlocking {
            argumentCaptor<OpenExternalBrowserAction> {
                verify(dispatcher).dispatch(capture())
            }
        }
    }

    private fun article(): FavoritableArticle = FavoritableArticle(
        id = 1,
        title = "title",
        url = "https://example.com/1",
        status = Article.UNREAD,
        point = "1",
        postedDate = 0,
        feedId = 1,
        feedTitle = "feed",
        feedIconPath = "icon",
        isFavorite = false
    )

    private fun openUrlActionCreator(dispatcher: Dispatcher, isOpenInternal: Boolean): OpenUrlActionCreator =
        OpenUrlActionCreator(
            dispatcher = dispatcher,
            preferenceHelper = mock { on { this.isOpenInternal } doReturn isOpenInternal }
        )

    @Test
    fun `when two panes are shown and internal option is enabled then open detail pane`(): Unit = runBlocking {
        val dispatcher = mock<Dispatcher>()
        openUrlActionCreator(dispatcher, isOpenInternal = true)
            .run(item = ArticleItem.Content(article()), isTwoPane = true)
        argumentCaptor<OpenDetailPaneAction> {
            verify(dispatcher).dispatch(capture())
            assertThat(firstValue.value).isEqualTo(SelectedArticle(1, "title", "https://example.com/1"))
        }
    }

    @Test
    fun `when two panes are shown and external option is enabled then open external browser`(): Unit = runBlocking {
        val dispatcher = mock<Dispatcher>()
        openUrlActionCreator(dispatcher, isOpenInternal = false)
            .run(item = ArticleItem.Content(article()), isTwoPane = true)
        argumentCaptor<OpenExternalBrowserAction> {
            verify(dispatcher).dispatch(capture())
            assertThat(firstValue.value).isEqualTo("https://example.com/1")
        }
    }

    @Test
    fun `when one pane is shown and internal option is enabled then open internal browser`(): Unit = runBlocking {
        val dispatcher = mock<Dispatcher>()
        openUrlActionCreator(dispatcher, isOpenInternal = true)
            .run(item = ArticleItem.Content(article()), isTwoPane = false)
        argumentCaptor<OpenInternalBrowserAction> {
            verify(dispatcher).dispatch(capture())
            assertThat(firstValue.value).isEqualTo("https://example.com/1")
        }
    }

    @Test
    fun `when advertisement is tapped in two panes then nothing is opened`(): Unit = runBlocking {
        val dispatcher = mock<Dispatcher>()
        openUrlActionCreator(dispatcher, isOpenInternal = true)
            .run(item = ArticleItem.Advertisement, isTwoPane = true)
        verifyNoInteractions(dispatcher)
    }
}
