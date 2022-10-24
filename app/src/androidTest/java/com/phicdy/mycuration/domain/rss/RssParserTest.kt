package com.phicdy.mycuration.domain.rss

import androidx.test.core.app.ApplicationProvider
import com.phicdy.mycuration.CoroutineTestRule
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.deleteAll
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.repository.Database
import com.phicdy.mycuration.util.UrlUtil
import com.squareup.sqldelight.android.AndroidSqliteDriver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class RssParserTest {

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private val callback = object : RssParseExecutor.RssParseCallback {
        override fun succeeded(rssUrl: String) {}

        override fun failed(reason: RssParseResult.FailedReason, url: String) {}
    }
    private lateinit var rssRepository: RssRepository
    private lateinit var parser: RssParser

    private val db = Database(
            AndroidSqliteDriver(
                    schema = Database.Schema,
                    context = ApplicationProvider.getApplicationContext(),
                    name = "rss_manage"
            )
    )

    @Before
    @Throws(Exception::class)
    fun setUp() {
        parser = RssParser()
        rssRepository = RssRepository(
                db,
                ArticleRepository(db, coroutineTestRule.testCoroutineDispatcherProvider, coroutineTestRule.testCoroutineScope),
                FilterRepository(db, coroutineTestRule.testCoroutineDispatcherProvider),
                coroutineTestRule.testCoroutineScope,
                coroutineTestRule.testCoroutineDispatcherProvider
        )
        deleteAll(db)
    }

    @After
    fun tearDown() {
        deleteAll(db)
    }

    @Test
    fun testParseFeedInfoRSS1() = coroutineTestRule.testCoroutineScope.runTest {
        val parser = RssParser()
        val executor = RssParseExecutor(parser, rssRepository)
        executor.start("https://news.yahoo.co.jp/rss/topics/top-picks.xml", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val addedFeed =
            rssRepository.getFeedByUrl("https://news.yahoo.co.jp/rss/topics/top-picks.xml")
        assertNotNull(addedFeed)
        assertEquals("https://news.yahoo.co.jp/rss/topics/top-picks.xml", addedFeed?.url)
        assertEquals("https://news.yahoo.co.jp/topics/top-picks?source=rss", addedFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed?.iconPath)

        //https://feed.japan.cnet.com/rss/index.rdf
        //https://itpro.nikkeibp.co.jp/rss/ITpro.rdf
        //https://blog.livedoor.jp/itsoku/index.rdf
        //https://sierblog.com/index.rdf
    }

    @Test
    fun testParseFeedInfoRSS1_rdf() = coroutineTestRule.testCoroutineScope.runTest {
        val testUrl = "https://b.hatena.ne.jp/hotentry/it.rss"
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
        assertEquals("https://b.hatena.ne.jp/hotentry/it", addedFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed?.iconPath)
    }

    @Test
    fun testParseFeedInfoRSS2() = coroutineTestRule.testCoroutineScope.runTest {
        val parser = RssParser()
        val executor = RssParseExecutor(parser, rssRepository)
        executor.start("https://hiroki.jp/feed/", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val addedFeed = rssRepository.getFeedByUrl("https://hiroki.jp/feed/")
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
        assertNotNull(infoqFeed)
        assertEquals("https://www.infoq.com/jp/feed", infoqFeed?.url)
        assertEquals("https://www.infoq.com/jp", infoqFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, infoqFeed?.iconPath)

        //https://blog.riywo.com/feed
        //https://dev.classmethod.jp/feed/
        //https://ggsoku.com/feed
        //https://labs.gree.jp/blog/feed
        //https://htcsok.info/feed/
        //https://developer.hatenastaff.com/rss
        //https://rss.rssad.jp/rss/itmtop/2.0/itmedia_all.xml
        //https://developers.linecorp.com/blog/ja/?feed=rss2
    }

    @Test
    fun testParseFeedInfoATOM() = coroutineTestRule.testCoroutineScope.runTest {
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
        executor.start("https://feeds.feedburner.com/blogspot/RLXA", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val googleTestFeed =
            rssRepository.getFeedByUrl("https://feeds.feedburner.com/blogspot/RLXA")
        assertNotNull(googleTestFeed)
        assertEquals("https://feeds.feedburner.com/blogspot/RLXA", googleTestFeed?.url)
        assertEquals("http://testing.googleblog.com/", googleTestFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, googleTestFeed?.iconPath)

        // MOONGIFT
        executor.start("https://feeds.feedburner.com/moongift", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val monngiftFeed = rssRepository.getFeedByUrl("https://feeds.feedburner.com/moongift")
        assertNotNull(monngiftFeed)
        assertEquals("https://feeds.feedburner.com/moongift", monngiftFeed?.url)
        assertEquals("http://www.moongift.jp/", monngiftFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, monngiftFeed?.iconPath)
    }

    @Test
    fun testParseFeedInfoTopHtml() = coroutineTestRule.testCoroutineScope.runTest {
        // Test top URL
        val parser = RssParser()
        val executor = RssParseExecutor(parser, rssRepository)
        executor.start("https://gigazine.net", callback)
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
    fun testParseFeedInfoTopHtmlFeedURLStartWithSlash() = coroutineTestRule.testCoroutineScope.runTest {
        // //smhn.info/feed is returned
        val parser = RssParser()
        val executor = RssParseExecutor(parser, rssRepository)
        executor.start("https://smhn.info", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val smhnFeed = rssRepository.getFeedByUrl("https://smhn.info/feed")

        assertNotNull(smhnFeed)
        assertEquals("https://smhn.info/feed", smhnFeed?.url)
        assertEquals("https://smhn.info", smhnFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, smhnFeed?.iconPath)
    }

    @Test
    fun testParseFeedInfoGzip() = coroutineTestRule.testCoroutineScope.runTest {
        val parser = RssParser()
        val executor = RssParseExecutor(parser, rssRepository)
        executor.start("https://ground-sesame.hatenablog.jp", callback)
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val surigomaFeed = rssRepository.getFeedByUrl("https://ground-sesame.hatenablog.jp/rss")
        assertNotNull(surigomaFeed)
        assertEquals("https://ground-sesame.hatenablog.jp/rss", surigomaFeed?.url)
        assertEquals("https://ground-sesame.hatenablog.jp/", surigomaFeed?.siteUrl)

        assertEquals(Feed.DEDAULT_ICON_PATH, surigomaFeed?.iconPath)
    }

    @Test
    fun testPathOnlyUrl() = coroutineTestRule.testCoroutineScope.runTest {
        addNewFeedAndCheckResult("https://b.hatena.ne.jp/hotentry/game",
                "https://b.hatena.ne.jp/hotentry/game.rss",
                "https://b.hatena.ne.jp/hotentry/game")
    }

    @Test
    fun testFeedPath() = coroutineTestRule.testCoroutineScope.runTest {
        addNewFeedAndCheckResult("https://www.a-kimama.com",
                "https://www.a-kimama.com/feed",
                "https://www.a-kimama.com")
    }

    @Test
    fun testNotFound() {
        val parser = RssParser()
        val url = "https://www.amazon.co.jp/"
        val result = parser.parseRssXml(UrlUtil.removeUrlParameter(url), true)
        assertThat(result.failedReason).isEqualTo(RssParseResult.FailedReason.NOT_FOUND)
    }

    @Test
    fun parseRSSVersion1AndFirstSizeIsCorrect() {
        val articles = parser.parseArticlesFromRss(RssV1().text().byteInputStream())
        assertThat(articles.size).isEqualTo(2)
    }

    @Test
    fun parserRSSVersion1AndFirstTitleIsCorrect() {
        val articles = parser.parseArticlesFromRss(RssV1().text().byteInputStream())
        assertThat(articles[0].title).isEqualTo("トップレベルのコンピュータエンジニアなら普段からチェックして当然の技術系メディアN選 - kuenishi's blog")
    }

    @Test
    fun parserRSSVersion1AndFirstURLIsCorrect() {
        val articles = parser.parseArticlesFromRss(RssV1().text().byteInputStream())
        assertThat(articles[0].url).isEqualTo("https://kuenishi.hatenadiary.jp/entry/2018/04/13/022908")
    }

    @Test
    fun parserRSSVersion1AndFirstDateIsCorrect() {
        val articles = parser.parseArticlesFromRss(RssV1().text().byteInputStream())
        assertThat(articles[0].postedDate).isEqualTo(1523554436000L)
    }

    @Test
    fun parserRSSVersion1AndFirstStatusIsunread() {
        val articles = parser.parseArticlesFromRss(RssV1().text().byteInputStream())
        assertThat(articles[0].status).isEqualTo(Article.UNREAD)
    }

    @Test
    fun parserRSSVersion1AndFirstHatenaPointIsMinus1() {
        val articles = parser.parseArticlesFromRss(RssV1().text().byteInputStream())
        assertThat(articles[0].point).isEqualTo("-1")
    }

    @Test
    fun parserRSSVersion1AndSecondTitleIsCorrect() {
        val articles = parser.parseArticlesFromRss(RssV1().text().byteInputStream())
        assertThat(articles[1].title).isEqualTo("「Excelが使える」のレベルを的確に見抜ける、入社試験に使えるサンプル問題が公開中【やじうまWatch】 - INTERNET Watch")
    }

    @Test
    fun parserRSSVersion1AndSecondURLIsCorrect() {
        val articles = parser.parseArticlesFromRss(RssV1().text().byteInputStream())
        assertThat(articles[1].url).isEqualTo("https://internet.watch.impress.co.jp/docs/yajiuma/1116878.html")
    }

    @Test
    fun parserRSSVersion1AndSecondDateIsCorrect() {
        val articles = parser.parseArticlesFromRss(RssV1().text().byteInputStream())
        assertThat(articles[1].postedDate).isEqualTo(1523567035000L)
    }

    @Test
    fun parserRSSVersion1AndSecondStatusIsunread() {
        val articles = parser.parseArticlesFromRss(RssV1().text().byteInputStream())
        assertThat(articles[1].status).isEqualTo(Article.UNREAD)
    }

    @Test
    fun parserRSSVersion1AndSecondHatenaPointIsMinus1() {
        val articles = parser.parseArticlesFromRss(RssV1().text().byteInputStream())
        assertThat(articles[1].point).isEqualTo("-1")
    }

    @Test
    fun parserRSSVersion2AndSizeIsCorrect() {
        val articles = parser.parseArticlesFromRss(RssV2().text().byteInputStream())
        assertThat(articles.size).isEqualTo(2)
    }

    @Test
    fun parserRSSVersion2AndFirstTitleIsCorrect() {
        val articles = parser.parseArticlesFromRss(RssV2().text().byteInputStream())
        assertThat(articles[0].title).isEqualTo("内閣支持率が続落し38% 時事")
    }

    @Test
    fun parserRSSVersion2AndFirstURLIsCorrect() {
        val articles = parser.parseArticlesFromRss(RssV2().text().byteInputStream())
        assertThat(articles[0].url).isEqualTo("https://news.yahoo.co.jp/pickup/6278905")
    }

    @Test
    fun parserRSSVersion2AndFirstDateIsCorrect() {
        val articles = parser.parseArticlesFromRss(RssV2().text().byteInputStream())
        assertThat(articles[0].postedDate).isEqualTo(1523607277000L)
    }

    @Test
    fun parserRSSVersion2AndFirstStatusIsunread() {
        val articles = parser.parseArticlesFromRss(RssV2().text().byteInputStream())
        assertThat(articles[0].status).isEqualTo(Article.UNREAD)
    }

    @Test
    fun parserRSSVersion2AndFirstHatenaPointIsMinus1() {
        val articles = parser.parseArticlesFromRss(RssV2().text().byteInputStream())
        assertThat(articles[0].point).isEqualTo("-1")
    }

    @Test
    fun parserRSSVersion2AndSecondTitleIsCorrect() {
        val articles = parser.parseArticlesFromRss(RssV2().text().byteInputStream())
        assertThat(articles[1].title).isEqualTo("ハム球場で迷惑行為 県警警戒")
    }

    @Test
    fun parserRSSVersion2AndSecondURLIsCorrect() {
        val articles = parser.parseArticlesFromRss(RssV2().text().byteInputStream())
        assertThat(articles[1].url).isEqualTo("https://news.yahoo.co.jp/pickup/6278920")
    }

    @Test
    fun parserRSSVersion2AndSecondDateIsCorrect() {
        val articles = parser.parseArticlesFromRss(RssV2().text().byteInputStream())
        assertThat(articles[1].postedDate).isEqualTo(1523612750000L)
    }

    @Test
    fun parserRSSVersion2AndSecondStatusIsunread() {
        val articles = parser.parseArticlesFromRss(RssV2().text().byteInputStream())
        assertThat(articles[1].status).isEqualTo(Article.UNREAD)
    }

    @Test
    fun parserRSSVersion2AndSecondHatenaPointIsMinus1() {
        val articles = parser.parseArticlesFromRss(RssV2().text().byteInputStream())
        assertThat(articles[1].point).isEqualTo("-1")
    }

    @Test
    fun parserAtom() {
        val articles = parser.parseArticlesFromRss(Atom().text().byteInputStream())
        assertThat(articles[0].url)
            .isEqualTo("https://feedproxy.google.com/~r/AndroidDagashi/~3/saI5mOCH5sg/57-2019-03-03")
    }

    @Test
    fun parserAtomAndroidDeveloperBlog() {
        val articles =
            parser.parseArticlesFromRss(AtomAndroidDeveloperBlog().text.byteInputStream())
        assertThat(articles[0].url)
            .isEqualTo("https://feedproxy.google.com/~r/blogspot/hsDu/~3/X3CHRsxGnbE/google-mobile-developer-day-at-game.html")
    }

    @Test
    fun parseFeedBurnerAndroidDeveloperBlog() {
        val result = parser.parseRssXml("https://feeds.feedburner.com/blogspot/hsDu", false)
        assertEquals("https://feeds.feedburner.com/blogspot/hsDu", result.feed?.url)
    }

    @Test
    fun parseArticlesOfFeedBurnerAndroidDeveloperBlog() {
        val articles =
            parser.parseArticlesFromRss(FeedBurnerAndroidDeveloperBlog().text.byteInputStream())
        assertThat(articles[0].url)
            .isEqualTo("https://android-developers.googleblog.com/2022/02/write-better-tests-with-new-testing.html")
    }

    private suspend fun addNewFeedAndCheckResult(
        testUrl: String,
        expectedFeedUrl: String,
        expectedSiteUrl: String
    ) {
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
