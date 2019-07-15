package com.phicdy.mycuration.articlelist.action

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.phicdy.mycuration.articlelist.store.ShareUrlStore
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.entity.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ShareUrlActionCreatorTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `share url of position of articles`() {
        runBlocking {
            val dispatcher = Dispatcher()
            val store = ShareUrlStore(dispatcher, Dispatchers.Unconfined)
            dispatcher.register(store)
            val article = mock<Article> {
                on { url } doReturn "aaa"
            }
            ShareUrlActionCreator(
                    dispatcher = dispatcher,
                    position = 0,
                    articles = listOf(article)
            ).run()
            assertThat(store.state.value).isEqualTo("aaa")
        }
    }

    @Test
    fun `when minus positon then return soon`() {
        runBlocking {
            val dispatcher = Dispatcher()
            val store = ShareUrlStore(dispatcher, Dispatchers.Unconfined)
            dispatcher.register(store)
            val article = mock<Article> {
                on { url } doReturn "aaa"
            }
            ShareUrlActionCreator(
                    dispatcher = dispatcher,
                    position = -1,
                    articles = listOf(article)
            ).run()
            assertThat(store.state.value).isNull()
        }
    }

    @Test
    fun `when positon exceeds the article list size then return soon`() {
        runBlocking {
            val dispatcher = Dispatcher()
            val store = ShareUrlStore(dispatcher, Dispatchers.Unconfined)
            dispatcher.register(store)
            val article = mock<Article> {
                on { url } doReturn "aaa"
            }
            ShareUrlActionCreator(
                    dispatcher = dispatcher,
                    position = 1,
                    articles = listOf(article)
            ).run()
            assertThat(store.state.value).isNull()
        }
    }
}