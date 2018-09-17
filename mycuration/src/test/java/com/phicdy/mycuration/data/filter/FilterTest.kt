package com.phicdy.mycuration.data.filter

import com.phicdy.mycuration.data.rss.Feed
import org.hamcrest.core.Is.`is`
import org.junit.Assert.*
import org.junit.Test

class FilterTest {
    @Test
    fun `when RSS list has a RSS then RSS title is the RSS's title`() {
        val filter = Filter(
                id = 1,
                feedId = 1,
                keyword = "aaa",
                title = "bbb",
                url = "http://www.google.com",
                feeds = arrayListOf(Feed(title = "title"))
        )
        assertThat(filter.feedTitle, `is`("title"))
    }

    @Test
    fun `when RSS list has two RSSes then RSS title is combined RSS title`() {
        val filter = Filter(
                id = 1,
                feedId = 1,
                keyword = "aaa",
                title = "bbb",
                url = "http://www.google.com",
                feeds = arrayListOf(
                        Feed(title = "title"),
                        Feed(title = "title2")
                )
        )
        assertThat(filter.feedTitle, `is`("title, title2"))
    }
}