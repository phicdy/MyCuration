package com.phicdy.mycuration.domain.rss

import android.support.test.runner.AndroidJUnit4

import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.util.UrlUtil

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import android.support.test.InstrumentationRegistry.getTargetContext
import com.phicdy.mycuration.data.rss.Article
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`

@RunWith(AndroidJUnit4::class)
class RssParserTest {

    private val callback = object : RssParseExecutor.RssParseCallback {
        override fun succeeded(rssUrl: String) {}

        override fun failed(@RssParseResult.FailedReason reason: Int, url: String) {}
    }
    private lateinit var adapter: DatabaseAdapter
    private lateinit var parser: RssParser

    @Before
    @Throws(Exception::class)
    fun setUp() {
        parser = RssParser()
        DatabaseAdapter.setUp(DatabaseHelper(getTargetContext()))
        adapter = DatabaseAdapter.getInstance()
        adapter.deleteAllArticles()
        adapter.deleteAll()
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        adapter.deleteAllArticles()
        adapter.deleteAll()
    }

    @Test
    fun testParseFeedInfoRSS1() {
        val parser = RssParser()
        val executor = RssParseExecutor(parser, DatabaseAdapter.getInstance())
        executor.start("http://news.yahoo.co.jp/pickup/rss.xml", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val addedFeed = adapter.getFeedByUrl("http://news.yahoo.co.jp/pickup/rss.xml")
        //		ArrayList<Feed> feeds = adapter.getAllFeedsWithNumOfUnreadArticles();
        assertNotNull(addedFeed)
        assertEquals("http://news.yahoo.co.jp/pickup/rss.xml", addedFeed.url)
        assertEquals("https://news.yahoo.co.jp/", addedFeed.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed.iconPath)

        //http://feed.japan.cnet.com/rss/index.rdf
        //http://itpro.nikkeibp.co.jp/rss/ITpro.rdf
        //http://blog.livedoor.jp/itsoku/index.rdf
        //http://sierblog.com/index.rdf
    }

    @Test
    fun testParseFeedInfoRSS1_rdf() {
        val testUrl = "http://b.hatena.ne.jp/hotentry/it.rss"
        val parser = RssParser()
        val executor = RssParseExecutor(parser, DatabaseAdapter.getInstance())
        executor.start(testUrl, callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val addedFeed = adapter.getFeedByUrl(testUrl)
        assertNotNull(addedFeed)
        assertEquals(testUrl, addedFeed.url)
        assertEquals("http://b.hatena.ne.jp/hotentry/it", addedFeed.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed.iconPath)
    }

    @Test
    fun testParseFeedInfoRSS2() {
        val parser = RssParser()
        val executor = RssParseExecutor(parser, DatabaseAdapter.getInstance())
        executor.start("http://hiroki.jp/feed/", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val addedFeed = adapter.getFeedByUrl("http://hiroki.jp/feed/")
        //		ArrayList<Feed> feeds = adapter.getAllFeedsWithNumOfUnreadArticles();
        assertNotNull(addedFeed)
        assertEquals("http://hiroki.jp/feed/", addedFeed.url)
        assertEquals("https://hiroki.jp", addedFeed.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed.iconPath)

        executor.start("http://www.infoq.com/jp/feed", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val infoqFeed = adapter.getFeedByUrl("http://www.infoq.com/jp/feed")
        //		ArrayList<Feed> feeds = adapter.getAllFeedsWithNumOfUnreadArticles();
        assertNotNull(infoqFeed)
        assertEquals("http://www.infoq.com/jp/feed", infoqFeed.url)
        assertEquals("http://www.infoq.com/jp/", infoqFeed.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, infoqFeed.iconPath)

        //http://blog.riywo.com/feed
        //http://dev.classmethod.jp/feed/
        //http://ggsoku.com/feed
        //http://labs.gree.jp/blog/feed
        //http://htcsoku.info/feed/
        //http://developer.hatenastaff.com/rss
        //http://rss.rssad.jp/rss/itmtop/2.0/itmedia_all.xml
        //http://developers.linecorp.com/blog/ja/?feed=rss2
    }

    @Test
    fun testParseFeedInfoATOM() {
        // Publickey
        val publicKeyFeedUrl = "http://www.publickey1.jp/atom.xml"
        val parser = RssParser()
        val executor = RssParseExecutor(parser, DatabaseAdapter.getInstance())
        executor.start(publicKeyFeedUrl, callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val publicKeyFeed = adapter.getFeedByUrl(publicKeyFeedUrl)
        assertNotNull(publicKeyFeed)
        assertEquals("Publickey", publicKeyFeed.title)
        assertEquals(publicKeyFeedUrl, publicKeyFeed.url)
        assertEquals("http://www.publickey1.jp/", publicKeyFeed.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, publicKeyFeed.iconPath)

        // Google testing blog
        executor.start("http://feeds.feedburner.com/blogspot/RLXA", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val googleTestFeed = adapter.getFeedByUrl("http://feeds.feedburner.com/blogspot/RLXA")
        assertNotNull(googleTestFeed)
        assertEquals("http://feeds.feedburner.com/blogspot/RLXA", googleTestFeed.url)
        assertEquals("http://testing.googleblog.com/", googleTestFeed.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, googleTestFeed.iconPath)

        // MOONGIFT
        executor.start("http://feeds.feedburner.com/moongift", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val monngiftFeed = adapter.getFeedByUrl("http://feeds.feedburner.com/moongift")
        assertNotNull(monngiftFeed)
        assertEquals("http://feeds.feedburner.com/moongift", monngiftFeed.url)
        assertEquals("http://www.moongift.jp/", monngiftFeed.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, monngiftFeed.iconPath)
    }

    @Test
    fun testParseFeedInfoTopHtml() {
        // Test top URL
        val parser = RssParser()
        val executor = RssParseExecutor(parser, DatabaseAdapter.getInstance())
        executor.start("http://gigazine.net", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val addedFeed = adapter.getFeedByUrl("https://gigazine.net/news/rss_2.0/")
        assertNotNull(addedFeed)
        assertEquals("https://gigazine.net/news/rss_2.0/", addedFeed.url)
        assertEquals("http://gigazine.net/", addedFeed.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed.iconPath)
    }

    @Test
    fun testParseFeedInfoTopHtml2() {
        val parser = RssParser()
        val executor = RssParseExecutor(parser, DatabaseAdapter.getInstance())
        executor.start("http://tech.mercari.com/", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val mercariFeed = adapter.getFeedByUrl("http://tech.mercari.com/rss")
        //		ArrayList<Feed> allFeeds = adapter.getAllFeedsThatHaveUnreadArticles();

        assertNotNull(mercariFeed)
        assertEquals("http://tech.mercari.com/rss", mercariFeed.url)
        assertEquals("http://tech.mercari.com/", mercariFeed.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, mercariFeed.iconPath)
    }

    @Test
    fun testParseFeedInfoTopHtmlFeedURLStartWithSlash() {
        // //smhn.info/feed is returned
        val parser = RssParser()
        val executor = RssParseExecutor(parser, DatabaseAdapter.getInstance())
        executor.start("http://smhn.info", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val smhnFeed = adapter.getFeedByUrl("http://smhn.info/feed")

        assertNotNull(smhnFeed)
        assertEquals("http://smhn.info/feed", smhnFeed.url)
        assertEquals("https://smhn.info", smhnFeed.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, smhnFeed.iconPath)
    }

    @Test
    fun testParseFeedInfoGzip() {
        val parser = RssParser()
        val executor = RssParseExecutor(parser, DatabaseAdapter.getInstance())
        executor.start("http://ground-sesame.hatenablog.jp", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val surigomaFeed = adapter.getFeedByUrl("http://ground-sesame.hatenablog.jp/rss")
        assertNotNull(surigomaFeed)
        assertEquals("http://ground-sesame.hatenablog.jp/rss", surigomaFeed.url)
        assertEquals("http://ground-sesame.hatenablog.jp/", surigomaFeed.siteUrl)

        assertEquals(Feed.DEDAULT_ICON_PATH, surigomaFeed.iconPath)
    }

    @Test
    fun testPathOnlyUrl() {
        addNewFeedAndCheckResult("http://b.hatena.ne.jp/hotentry/game",
                "http://b.hatena.ne.jp/hotentry/game.rss",
                "http://b.hatena.ne.jp/hotentry/game")
    }

    @Test
    fun testFeedPath() {
        addNewFeedAndCheckResult("https://www.a-kimama.com",
                "https://www.a-kimama.com/feed",
                "https://www.a-kimama.com")
    }

    @Test
    fun testNotFound() {
        val parser = RssParser()
        val url = "https://www.amazon.co.jp/"
        val result = parser.parseRssXml(UrlUtil.removeUrlParameter(url), true)
        assertThat(result.failedReason, `is`(RssParseResult.NOT_FOUND))
    }

    @Test
    fun parseRSSVersion1AndFirstSizeIsCorrect(){
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles.size, `is`(2))
    }

    @Test
    fun parserRSSVersion1AndFirstTitleIsCorrect() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[0].title, `is`("トップレベルのコンピュータエンジニアなら普段からチェックして当然の技術系メディアN選 - kuenishi's blog"))
    }

    @Test
    fun parserRSSVersion1AndFirstURLIsCorrect() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[0].url, `is`("https://kuenishi.hatenadiary.jp/entry/2018/04/13/022908"))
    }

    @Test
    fun parserRSSVersion1AndFirstDateIsCorrect() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[0].postedDate, `is`(1523554436000L))
    }

    @Test
    fun parserRSSVersion1AndFirstStatusIsunread() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[0].status, `is`(Article.UNREAD))
    }

    @Test
    fun parserRSSVersion1AndFirstHatenaPointIsMinus1() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[0].point, `is`("-1"))
    }

    @Test
    fun parserRSSVersion1AndSecondTitleIsCorrect() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[1].title, `is`("「Excelが使える」のレベルを的確に見抜ける、入社試験に使えるサンプル問題が公開中【やじうまWatch】 - INTERNET Watch"))
    }

    @Test
    fun parserRSSVersion1AndSecondURLIsCorrect() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[1].url, `is`("https://internet.watch.impress.co.jp/docs/yajiuma/1116878.html"))
    }

    @Test
    fun parserRSSVersion1AndSecondDateIsCorrect() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[1].postedDate, `is`(1523567035000L))
    }

    @Test
    fun parserRSSVersion1AndSecondStatusIsunread() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[1].status, `is`(Article.UNREAD))
    }

    @Test
    fun parserRSSVersion1AndSecondHatenaPointIsMinus1() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[1].point, `is`("-1"))
    }

    @Test
    fun parserRSSVersion2AndSizeIsCorrect() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles.size, `is`(2))
    }

    @Test
    fun parserRSSVersion2AndFirstTitleIsCorrect() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[0].title, `is`("内閣支持率が続落し38% 時事"))
    }

    @Test
    fun parserRSSVersion2AndFirstURLIsCorrect() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[0].url, `is`("https://news.yahoo.co.jp/pickup/6278905"))
    }

    @Test
    fun parserRSSVersion2AndFirstDateIsCorrect() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[0].postedDate, `is`(1523607277000L))
    }

    @Test
    fun parserRSSVersion2AndFirstStatusIsunread() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[0].status, `is`(Article.UNREAD))
    }

    @Test
    fun parserRSSVersion2AndFirstHatenaPointIsMinus1() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[0].point, `is`("-1"))
    }

    @Test
    fun parserRSSVersion2AndSecondTitleIsCorrect() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[1].title, `is`("ハム球場で迷惑行為 県警警戒"))
    }

    @Test
    fun parserRSSVersion2AndSecondURLIsCorrect() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[1].url, `is`("https://news.yahoo.co.jp/pickup/6278920"))
    }

    @Test
    fun parserRSSVersion2AndSecondDateIsCorrect() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[1].postedDate, `is`(1523612750000L))
    }

    @Test
    fun parserRSSVersion2AndSecondStatusIsunread() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[1].status, `is`(Article.UNREAD))
    }

    @Test
    fun parserRSSVersion2AndSecondHatenaPointIsMinus1() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[1].point, `is`("-1"))
    }

    private fun addNewFeedAndCheckResult(testUrl: String, expectedFeedUrl: String, expectedSiteUrl: String) {
        val parser = RssParser()
        val executor = RssParseExecutor(parser, DatabaseAdapter.getInstance())
        executor.start(testUrl, callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val addedFeed = adapter.getFeedByUrl(expectedFeedUrl)
        assertNotNull(addedFeed)
        assertEquals(expectedFeedUrl, addedFeed.url)
        assertEquals(expectedSiteUrl, addedFeed.siteUrl)

        assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed.iconPath)
    }
}
