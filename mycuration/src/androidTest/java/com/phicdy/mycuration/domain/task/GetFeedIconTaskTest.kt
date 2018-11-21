package com.phicdy.mycuration.domain.task

import android.support.test.InstrumentationRegistry.getTargetContext
import android.support.test.runner.AndroidJUnit4
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.rss.Feed
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
        DatabaseAdapter.setUp(DatabaseHelper(getTargetContext()))
    }

    @After
    fun tearDown() {
    }

    @Test
    fun iconExistsWhenGetKindouIcon() {
        val kindou = "http://kindou.info"
        DatabaseAdapter.getInstance().saveNewFeed("kindou", kindou, Feed.ATOM, kindou)
        val task = GetFeedIconTask()
        task.execute(kindou)
        Thread.sleep(3000)

        val rss = DatabaseAdapter.getInstance().getFeedByUrl(kindou)
        assertThat(rss.iconPath, `is`("https://kindou.info/img/favicon.ico"))
    }

    @Test
    fun iconDoesNotExistWhenGetGreeBlogIcon() {
        val gree = "http://labs.gree.jp/blog"
        DatabaseAdapter.getInstance().saveNewFeed("gree", gree, Feed.ATOM, gree)
        val greeBlogIconTask = GetFeedIconTask()
        greeBlogIconTask.execute(gree)
        Thread.sleep(3000)

        val rss = DatabaseAdapter.getInstance().getFeedByUrl(gree)
        assertThat(rss.iconPath, `is`(Feed.DEDAULT_ICON_PATH))
    }
}
