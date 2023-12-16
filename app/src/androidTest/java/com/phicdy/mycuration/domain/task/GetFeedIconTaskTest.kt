package com.phicdy.mycuration.domain.task

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.phicdy.test.util.CoroutineTestRule
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.repository.Database
import com.squareup.sqldelight.android.AndroidSqliteDriver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class GetFeedIconTaskTest {

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var rssRepository: RssRepository

    @Before
    fun setup() {
        val db = Database(
                AndroidSqliteDriver(
                        schema = Database.Schema,
                        context = ApplicationProvider.getApplicationContext(),
                        name = "rss_manage"
                )
        )
        rssRepository = RssRepository(db, ArticleRepository(db, coroutineTestRule.testCoroutineDispatcherProvider, coroutineTestRule.testCoroutineScope), FilterRepository(db, coroutineTestRule.testCoroutineDispatcherProvider), coroutineTestRule.testCoroutineScope, coroutineTestRule.testCoroutineDispatcherProvider)
    }

    @Test
    fun iconExistsWhenGetKindouIcon() = coroutineTestRule.testCoroutineScope.runTest {
        val kindou = "http://kindou.info"
        rssRepository.store("kindou", kindou, Feed.ATOM, kindou)
        val task = GetFeedIconTask()
        val iconPath = task.execute(kindou)
        assertThat(iconPath, `is`("https://kindou.info/img/favicon.ico"))
    }
}
