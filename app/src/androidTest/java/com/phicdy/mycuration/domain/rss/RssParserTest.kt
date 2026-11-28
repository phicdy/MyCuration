package com.phicdy.mycuration.domain.rss

import androidx.test.core.app.ApplicationProvider
import com.phicdy.test.util.CoroutineTestRule
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.deleteAll
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import com.phicdy.mycuration.repository.Database
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.GzipSink
import okio.buffer
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

    @get:Rule
    val server = MockWebServer()

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
        serve("/rss1.rdf" to feed(RSS1))

        addNewFeedAndCheckResult(urlOf("/rss1.rdf"), urlOf("/rss1.rdf"), SITE_URL)
    }

    @Test
    fun testParseFeedInfoRSS2() = coroutineTestRule.testCoroutineScope.runTest {
        serve("/rss2" to feed(rss2(SITE_URL)), "/no-site-link" to feed(rss2()))

        addNewFeedAndCheckResult(urlOf("/rss2"), urlOf("/rss2"), SITE_URL)
        // The site URL is the protocol, host and port of the feed URL if the feed has no site link
        addNewFeedAndCheckResult(urlOf("/no-site-link"), urlOf("/no-site-link"), "http://${server.hostName}:${server.port}")
    }

    @Test
    fun testParseFeedInfoATOM() = coroutineTestRule.testCoroutineScope.runTest {
        serve("/atom.xml" to feed(ATOM))
        val executor = RssParseExecutor(RssParser(), rssRepository)
        executor.start(urlOf("/atom.xml"), callback)

        val atomFeed = rssRepository.getFeedByUrl(urlOf("/atom.xml"))
        assertNotNull(atomFeed)
        assertEquals("Publickey", atomFeed?.title)
        assertEquals(urlOf("/atom.xml"), atomFeed?.url)
        assertEquals(SITE_URL, atomFeed?.siteUrl)
        assertEquals(Feed.DEDAULT_ICON_PATH, atomFeed?.iconPath)
    }

    @Test
    fun testParseFeedInfoTopHtml() = coroutineTestRule.testCoroutineScope.runTest {
        serve("/" to page(rssLink(urlOf("/news/rss_2.0/"))), "/news/rss_2.0/" to feed(rss2(SITE_URL)))

        addNewFeedAndCheckResult(urlOf("/"), urlOf("/news/rss_2.0/"), SITE_URL)
    }

    @Test
    fun testParseFeedInfoTopHtmlFeedURLStartWithSlash() = coroutineTestRule.testCoroutineScope.runTest {
        // Like smhn.info, the link is "//<host>/<path>"
        serve("/" to page(rssLink("//${server.hostName}:${server.port}/rss")), "/rss" to feed(rss2(SITE_URL)))

        addNewFeedAndCheckResult(urlOf("/"), urlOf("/rss"), SITE_URL)
    }

    @Test
    fun testParseFeedInfoGzip() = coroutineTestRule.testCoroutineScope.runTest {
        serve("/" to page(rssLink("/rss")), "/rss" to gzipFeed(rss2(SITE_URL)))

        addNewFeedAndCheckResult(urlOf("/"), urlOf("/rss"), SITE_URL)
    }

    @Test
    fun testPathOnlyUrl() = coroutineTestRule.testCoroutineScope.runTest {
        serve("/hotentry/game" to page(rssLink("/hotentry/game.rss")), "/hotentry/game.rss" to feed(rss2(SITE_URL)))

        addNewFeedAndCheckResult(urlOf("/hotentry/game"), urlOf("/hotentry/game.rss"), SITE_URL)
    }

    @Test
    fun testCanonicalPathOnlyUrl() = coroutineTestRule.testCoroutineScope.runTest {
        serve(
            "/amp/article" to page("""<link rel="canonical" href="/article">"""),
            "/article" to page(rssLink("/article.rss")),
            "/article.rss" to feed(rss2(SITE_URL))
        )

        addNewFeedAndCheckResult(urlOf("/amp/article"), urlOf("/article.rss"), SITE_URL)
    }

    @Test
    fun testFeedPath() = coroutineTestRule.testCoroutineScope.runTest {
        // No RSS link in the page, so "/feed" is tried
        serve("/" to page(""), "/feed" to feed(rss2(SITE_URL)))

        addNewFeedAndCheckResult(urlOf("/"), urlOf("/feed"), SITE_URL)
    }

    @Test
    fun testNotFound() {
        // No RSS link in the page and "/feed" returns 404
        serve("/" to page(""))
        val parser = RssParser()
        val result = parser.parseRssXml(urlOf("/"), true)
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
        serve("/blogspot/hsDu" to feed(FeedBurnerAndroidDeveloperBlog().text))
        val result = parser.parseRssXml(urlOf("/blogspot/hsDu"), false)
        assertEquals(urlOf("/blogspot/hsDu"), result.feed?.url)
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

        val addedFeed = rssRepository.getFeedByUrl(expectedFeedUrl)
        assertNotNull(addedFeed)
        assertEquals(expectedFeedUrl, addedFeed?.url)
        assertEquals(expectedSiteUrl, addedFeed?.siteUrl)

        assertEquals(Feed.DEDAULT_ICON_PATH, addedFeed?.iconPath)
    }

    private fun urlOf(path: String): String = server.url(path).toString()

    private fun serve(vararg routes: Pair<String, MockResponse>) {
        val responses = mapOf(*routes)
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                responses[request.path] ?: MockResponse().setResponseCode(404)
        }
    }

    private fun feed(xml: String): MockResponse =
        MockResponse().setHeader("Content-Type", XML_CONTENT_TYPE).setBody(xml)

    private fun gzipFeed(xml: String): MockResponse =
        MockResponse()
            .setHeader("Content-Type", XML_CONTENT_TYPE)
            .setHeader("Content-Encoding", "gzip")
            .setBody(Buffer().apply { GzipSink(this).buffer().use { it.writeUtf8(xml) } })

    private fun page(head: String): MockResponse =
        MockResponse()
            .setHeader("Content-Type", "text/html; charset=utf-8")
            .setBody("<html><head>$head</head><body></body></html>")

    private fun rssLink(href: String): String =
        """<link rel="alternate" type="application/rss+xml" title="feed" href="$href">"""

    private fun rss2(siteUrl: String? = null): String =
        """<?xml version="1.0" encoding="UTF-8"?><rss version="2.0"><channel><title>RSS 2.0</title>""" +
            siteUrl?.let { "<link>$it</link>" }.orEmpty() +
            "</channel></rss>"

    companion object {
        private const val SITE_URL = "https://example.com/"
        private const val XML_CONTENT_TYPE = "application/xml; charset=utf-8"
        private const val RSS1 = """<?xml version="1.0" encoding="UTF-8"?>
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://purl.org/rss/1.0/">
<channel rdf:about="$SITE_URL"><title>RSS 1.0</title><link>$SITE_URL</link></channel>
</rdf:RDF>"""
        private const val ATOM = """<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom"><title>Publickey</title><link rel="alternate" href="$SITE_URL"/></feed>"""
    }
}
