package com.phicdy.mycuration.articlelist.action

import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.articlelist.store.OpenExternalWebBrowserStateStore
import com.phicdy.mycuration.articlelist.store.OpenInternalWebBrowserStateStore
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.entity.FavoritableArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

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
}