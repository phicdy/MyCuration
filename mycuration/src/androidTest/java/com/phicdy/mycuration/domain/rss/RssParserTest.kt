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
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.data.rss.Article
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

@RunWith(AndroidJUnit4::class)
class RssParserTest {

    private val callback = object : RssParseExecutor.RssParseCallback {
        override fun succeeded(rssUrl: String) {}

        override fun failed(reason: RssParseResult.FailedReason, url: String) {}
    }
    private lateinit var adapter: DatabaseAdapter
    private lateinit var rssRepository: RssRepository
    private lateinit var parser: RssParser

    @Before
    @Throws(Exception::class)
    fun setUp() {
        parser = RssParser()
        val helper = DatabaseHelper(getTargetContext())
        DatabaseAdapter.setUp(helper)
        adapter = DatabaseAdapter.getInstance()
        rssRepository = RssRepository(
                helper.writableDatabase,
                ArticleRepository(helper.writableDatabase),
                FilterRepository(helper.writableDatabase)
        )
        adapter.deleteAll()
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        adapter.deleteAll()
    }

    @Test
    fun testParseFeedInfoRSS1() = runBlocking {
        val parser = RssParser()
        val executor = RssParseExecutor(parser, rssRepository)
        executor.start("http://news.yahoo.co.jp/pickup/rss.xml", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val addedFeed = rssRepository.getFeedByUrl("http://news.yahoo.co.jp/pickup/rss.xml")
        //		ArrayList<Feed> feeds = adapter.getAllFeedsWithNumOfUnreadArticles();
        assertNotNull(addedFeed)
        assertEquals("http://news.yahoo.co.jp/pickup/rss.xml", addedFeed?.url)
        assertEquals("https://news.yahoo.co.jp/", addedFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed?.iconPath)

        //http://feed.japan.cnet.com/rss/index.rdf
        //http://itpro.nikkeibp.co.jp/rss/ITpro.rdf
        //http://blog.livedoor.jp/itsoku/index.rdf
        //http://sierblog.com/index.rdf
    }

    @Test
    fun testParseFeedInfoRSS1_rdf() = runBlocking {
        val testUrl = "http://b.hatena.ne.jp/hotentry/it.rss"
        val parser = RssParser()
        val executor = RssParseExecutor(parser, rssRepository)
        executor.start(testUrl, callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val addedFeed = rssRepository.getFeedByUrl(testUrl)
        assertNotNull(addedFeed)
        assertEquals(testUrl, addedFeed?.url)
        assertEquals("http://b.hatena.ne.jp/hotentry/it", addedFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed?.iconPath)
    }

    @Test
    fun testParseFeedInfoRSS2() = runBlocking {
        val parser = RssParser()
        val executor = RssParseExecutor(parser, rssRepository)
        executor.start("https://hiroki.jp/feed/", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val addedFeed = rssRepository.getFeedByUrl("https://hiroki.jp/feed/")
        //		ArrayList<Feed> feeds = adapter.getAllFeedsWithNumOfUnreadArticles();
        assertNotNull(addedFeed)
        assertEquals("https://hiroki.jp/feed/", addedFeed?.url)
        assertEquals("https://hiroki.jp", addedFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed?.iconPath)

        executor.start("https://www.infoq.com/jp/feed", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val infoqFeed = rssRepository.getFeedByUrl("https://www.infoq.com/jp/feed")
        //		ArrayList<Feed> feeds = adapter.getAllFeedsWithNumOfUnreadArticles();
        assertNotNull(infoqFeed)
        assertEquals("https://www.infoq.com/jp/feed", infoqFeed?.url)
        assertEquals("https://www.infoq.com/jp", infoqFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, infoqFeed?.iconPath)

        //http://blog.riywo.com/feed
        //http://dev.classmethod.jp/feed/
        //http://ggsoku.com/feed
        //http://labs.gree.jp/blog/feed
        //http://htcsok.info/feed/
        //http://developer.hatenastaff.com/rss
        //http://rss.rssad.jp/rss/itmtop/2.0/itmedia_all.xml
        //http://developers.linecorp.com/blog/ja/?feed=rss2
    }

    @Test
    fun testParseFeedInfoATOM() = runBlocking {
        // Publickey
        val publicKeyFeedUrl = "https://www.publickey1.jp/atom.xml"
        val parser = RssParser()
        val executor = RssParseExecutor(parser, rssRepository)
        executor.start(publicKeyFeedUrl, callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val publicKeyFeed = rssRepository.getFeedByUrl(publicKeyFeedUrl)
        assertNotNull(publicKeyFeed)
        assertEquals("Publickey", publicKeyFeed?.title)
        assertEquals(publicKeyFeedUrl, publicKeyFeed?.url)
        assertEquals("https://www.publickey1.jp/", publicKeyFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, publicKeyFeed?.iconPath)

        // Google testing blog
        executor.start("http://feeds.feedburner.com/blogspot/RLXA", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val googleTestFeed = rssRepository.getFeedByUrl("http://feeds.feedburner.com/blogspot/RLXA")
        assertNotNull(googleTestFeed)
        assertEquals("http://feeds.feedburner.com/blogspot/RLXA", googleTestFeed?.url)
        assertEquals("http://testing.googleblog.com/", googleTestFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, googleTestFeed?.iconPath)

        // MOONGIFT
        executor.start("http://feeds.feedburner.com/moongift", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val monngiftFeed = rssRepository.getFeedByUrl("http://feeds.feedburner.com/moongift")
        assertNotNull(monngiftFeed)
        assertEquals("http://feeds.feedburner.com/moongift", monngiftFeed?.url)
        assertEquals("http://www.moongift.jp/", monngiftFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, monngiftFeed?.iconPath)
    }

    @Test
    fun testParseFeedInfoTopHtml() = runBlocking {
        // Test top URL
        val parser = RssParser()
        val executor = RssParseExecutor(parser, rssRepository)
        executor.start("http://gigazine.net", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val addedFeed = rssRepository.getFeedByUrl("https://gigazine.net/news/rss_2.0/")
        assertNotNull(addedFeed)
        assertEquals("https://gigazine.net/news/rss_2.0/", addedFeed?.url)
        assertEquals("https://gigazine.net/", addedFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed?.iconPath)
    }

    @Test
    fun testParseFeedInfoTopHtml2() = runBlocking {
        val parser = RssParser()
        val executor = RssParseExecutor(parser, rssRepository)
        executor.start("http://tech.mercari.com/", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val mercariFeed = rssRepository.getFeedByUrl("https://tech.mercari.com/rss")
        //		ArrayList<Feed> allFeeds = adapter.getAllFeedsThatHaveUnreadArticles();

        assertNotNull(mercariFeed)
        assertEquals("https://tech.mercari.com/rss", mercariFeed?.url)
        assertEquals("https://tech.mercari.com/", mercariFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, mercariFeed?.iconPath)
    }

    @Test
    fun testParseFeedInfoTopHtmlFeedURLStartWithSlash() = runBlocking {
        // //smhn.info/feed is returned
        val parser = RssParser()
        val executor = RssParseExecutor(parser, rssRepository)
        executor.start("http://smhn.info", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val smhnFeed = rssRepository.getFeedByUrl("http://smhn.info/feed")

        assertNotNull(smhnFeed)
        assertEquals("http://smhn.info/feed", smhnFeed?.url)
        assertEquals("https://smhn.info", smhnFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, smhnFeed?.iconPath)
    }

    @Test
    fun testParseFeedInfoGzip() = runBlocking {
        val parser = RssParser()
        val executor = RssParseExecutor(parser, rssRepository)
        executor.start("http://ground-sesame.hatenablog.jp", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val surigomaFeed = rssRepository.getFeedByUrl("http://ground-sesame.hatenablog.jp/rss")
        assertNotNull(surigomaFeed)
        assertEquals("http://ground-sesame.hatenablog.jp/rss", surigomaFeed?.url)
        assertEquals("http://ground-sesame.hatenablog.jp/", surigomaFeed?.siteUrl)

        assertEquals(Feed.DEDAULT_ICON_PATH, surigomaFeed?.iconPath)
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
        assertThat(result.failedReason, `is`(RssParseResult.FailedReason.NOT_FOUND))
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

    private fun addNewFeedAndCheckResult(testUrl: String, expectedFeedUrl: String, expectedSiteUrl: String) = runBlocking {
        val parser = RssParser()
        val executor = RssParseExecutor(parser, rssRepository)
        executor.start(testUrl, callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val addedFeed = rssRepository.getFeedByUrl(expectedFeedUrl)
        assertNotNull(addedFeed)
        assertEquals(expectedFeedUrl, addedFeed?.url)
        assertEquals(expectedSiteUrl, addedFeed?.siteUrl)

        assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed?.iconPath)
    }
}
