package com.phicdy.mycuration.articlelist.action

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.phicdy.mycuration.articlelist.store.OpenExternalWebBrowserStateStore
import com.phicdy.mycuration.articlelist.store.OpenInternalWebBrowserStateStore
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.preference.PreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Test

@ExperimentalCoroutinesApi
class OpenUrlActionCreatorTest {

    @Test
    fun `when internal option is enabled then open internal browser`() {
        runBlocking {
            val preferenceHelper = mock<PreferenceHelper> {
                on { isOpenInternal } doReturn true
            }
            val store = mock<OpenInternalWebBrowserStateStore> {
                on { coroutineContext } doReturn Dispatchers.Unconfined
            }
            val dispatcher = Dispatcher()
            dispatcher.register(store)
            OpenUrlActionCreator(
                    dispatcher = dispatcher,
                    preferenceHelper = preferenceHelper,
                    feedId = 0,
                    article = mock { on { copy(feedTitle = "") } doReturn mock() },
                    rssRepository = mock()
            ).run()
            verify(store).notify(any())
        }
    }

    @Test
    fun `when external option is enabled then open external browser`() {
        runBlocking {
            val preferenceHelper = mock<PreferenceHelper> {
                on { isOpenInternal } doReturn false
            }
            val store = mock<OpenExternalWebBrowserStateStore> {
                on { coroutineContext } doReturn Dispatchers.Unconfined
            }
            val dispatcher = Dispatcher()
            dispatcher.register(store)
            OpenUrlActionCreator(
                    dispatcher = dispatcher,
                    preferenceHelper = preferenceHelper,
                    feedId = 0,
                    article = mock { on { copy(feedTitle = "") } doReturn mock() },
                    rssRepository = mock()
            ).run()
            verify(store).notify(any())
        }
    }
}