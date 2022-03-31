package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.RssRepository
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class EditRssTitleActionCreatorTest {
    @Test
    fun editEmptyTitle() {
        val dispatcher = mock<Dispatcher>()
        runBlocking {
            EditRssTitleActionCreator(dispatcher, mock()).run("", 0)
        }
        runBlocking {
            argumentCaptor<EditRssTitleErrorAction> {
                verify(dispatcher).dispatch(capture())
                assertThat(firstValue.value).isEqualTo(RssListMessage.Type.ERROR_EMPTY_RSS_TITLE_EDIT)
            }
        }
    }

    @Test
    fun succeedToEdit() {
        val dispatcher = mock<Dispatcher>()
        runBlocking {
            val repository = mock<RssRepository> {
                onBlocking { saveNewTitle(0, "title") } doReturn 1
            }
            EditRssTitleActionCreator(dispatcher, repository).run("title", 0)
        }
        runBlocking {
            argumentCaptor<EditRssTitleSuccessAction> {
                verify(dispatcher).dispatch(capture())
                assertThat(firstValue.value).isEqualTo(EditRssTitleValue("title", 0))
            }
        }
    }
}