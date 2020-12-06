package com.phicdy.mycuration.articlelist.action

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
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