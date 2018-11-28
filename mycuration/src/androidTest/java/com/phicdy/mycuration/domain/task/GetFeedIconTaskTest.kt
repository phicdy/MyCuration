package com.phicdy.mycuration.domain.task

import android.support.test.runner.AndroidJUnit4
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.rss.Feed
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GetFeedIconTaskTest {

    @Before
    fun setup() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun iconExistsWhenGetKindouIcon() = runBlocking {
        val kindou = "http://kindou.info"
        DatabaseAdapter.getInstance().saveNewFeed("kindou", kindou, Feed.ATOM, kindou)
        val task = GetFeedIconTask()
        val iconPath = task.execute(kindou)
        assertThat(iconPath, `is`("https://kindou.info/img/favicon.ico"))
    }

    @Test
    fun iconDoesNotExistWhenGetGreeBlogIcon() = runBlocking {
        val gree = "http://labs.gree.jp/blog"
        DatabaseAdapter.getInstance().saveNewFeed("gree", gree, Feed.ATOM, gree)
        val greeBlogIconTask = GetFeedIconTask()
        val iconPath = greeBlogIconTask.execute(gree)
        assertThat(iconPath, `is`(""))
    }
}
