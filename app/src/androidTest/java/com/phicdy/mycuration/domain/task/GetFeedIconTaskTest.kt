package com.phicdy.mycuration.domain.task

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GetFeedIconTaskTest {

    private lateinit var rssRepository: RssRepository

    @Before
    fun setup() {
        val db = DatabaseHelper(ApplicationProvider.getApplicationContext()).writableDatabase
        rssRepository = RssRepository(db, ArticleRepository(db), FilterRepository(db))
    }

    @After
    fun tearDown() {
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
