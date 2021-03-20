package com.phicdy.mycuration.domain.task

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.phicdy.mycuration.TestCoroutineDispatcherProvider
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.db.DatabaseMigration
import com.phicdy.mycuration.data.db.ResetIconPathTask
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class GetFeedIconTaskTest {

    private val testCoroutineScope = TestCoroutineScope()
    private val testDispatcher = TestCoroutineDispatcher()
    private val testDispatcherProvider = TestCoroutineDispatcherProvider(testDispatcher)

    private lateinit var rssRepository: RssRepository

    @Before
    fun setup() {
        val db = DatabaseHelper(ApplicationProvider.getApplicationContext(), DatabaseMigration(ResetIconPathTask())).writableDatabase
        rssRepository = RssRepository(db, ArticleRepository(db, testDispatcherProvider), FilterRepository(db, testDispatcherProvider), testCoroutineScope, testDispatcherProvider)
    }

    @After
    fun tearDown() {
        testCoroutineScope.cleanupTestCoroutines()
    }

    @Test
    fun iconExistsWhenGetKindouIcon() = runBlocking {
        val kindou = "http://kindou.info"
        rssRepository.store("kindou", kindou, Feed.ATOM, kindou)
        val task = GetFeedIconTask()
        val iconPath = task.execute(kindou)
        assertThat(iconPath, `is`("https://kindou.info/img/favicon.ico"))
    }
}
