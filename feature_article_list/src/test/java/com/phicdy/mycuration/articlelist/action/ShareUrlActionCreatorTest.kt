package com.phicdy.mycuration.articlelist.action

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.phicdy.mycuration.articlelist.ArticleItem
import com.phicdy.mycuration.articlelist.store.ShareUrlStore
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.entity.FavoritableArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

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
            val article = mock<FavoritableArticle> {
                on { url } doReturn "aaa"
            }
            ShareUrlActionCreator(
                    dispatcher = dispatcher
            ).run(
                    position = 0,
                    items = listOf(ArticleItem.Content(article))
            )
            assertThat(store.state.value).isEqualTo("aaa")
        }
    }

    @Test
    fun `when minus positon then return soon`() {
        runBlocking {
            val dispatcher = Dispatcher()
            val store = ShareUrlStore(dispatcher, Dispatchers.Unconfined)
            dispatcher.register(store)
            val article = mock<FavoritableArticle> {
                on { url } doReturn "aaa"
            }
            ShareUrlActionCreator(
                    dispatcher = dispatcher
            ).run(
                    position = -1,
                    items = listOf(ArticleItem.Content(article))
            )
            assertThat(store.state.value).isNull()
        }
    }

    @Test
    fun `when positon exceeds the article list size then return soon`() {
        runBlocking {
            val dispatcher = Dispatcher()
            val store = ShareUrlStore(dispatcher, Dispatchers.Unconfined)
            dispatcher.register(store)
            val article = mock<FavoritableArticle> {
                on { url } doReturn "aaa"
            }
            ShareUrlActionCreator(
                    dispatcher = dispatcher
            ).run(
                    position = 1,
                    items = listOf(ArticleItem.Content(article))
            )
            assertThat(store.state.value).isNull()
        }
    }
}