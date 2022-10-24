package com.phicdy.mycuration.domain.rss

import com.phicdy.mycuration.domain.util.DateParser
import com.phicdy.mycuration.domain.util.TextUtil
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.Feed
import org.jsoup.Jsoup
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject

class RssParser @Inject constructor() {

    private var isCanonical = false

    private fun parse(canonicalUrl: String): RssParseResult {
        if (isCanonical) {
            return RssParseResult(failedReason = RssParseResult.FailedReason.NOT_FOUND)
        }
        isCanonical = true
        return parseRssXml(canonicalUrl, false)
    }

    /**
     * Try to parse RSS (RSS 1.0/2.0 and ATOM) from the URL.
     * If the URL is HTML URL, search RSS URL from link tag like below.
     * <link rel="alternate" type="application/rss+xml" title="TechCrunch Japan &raquo; フィード" href="http://jp.techcrunch.com/feed/"></link>
     *
     * @param baseUrl URL to parse. RSS URL or HTML URL.
     * @param checkCanonical If true, check canonical setting in the page like below
     * <link rel="canonical" href="http://xxxxxxxx"></link>
     * @return RssParseResult. If not found or error occurs, feed instance in the result is null.
     */
    fun parseRssXml(baseUrl: String, checkCanonical: Boolean): RssParseResult {
        try {
            val url = URL(baseUrl)
            if (!"http".equals(url.protocol, ignoreCase = true) && !"https".equals(url.protocol, ignoreCase = true)) {
                return RssParseResult(failedReason = RssParseResult.FailedReason.INVALID_URL)
            }
            val pcUserAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.63 Safari/537.36"
            val document = Jsoup.connect(baseUrl).userAgent(pcUserAgent).get()
            if (!document.getElementsByTag("rdf").isEmpty() || !document.getElementsByTag("rdf:rdf").isEmpty()) {
                // RSS 1.0
                val links = document.getElementsByTag("link")
                var siteUrl = ""
                links.forEach { element ->
                    if (element.parent()?.tag().toString() == "channel") {
                        siteUrl = element.text()
                        return@forEach
                    }
                }
                if (siteUrl.isBlank()) {
                    siteUrl = url.protocol + "://" + url.host
                }
                val title = document.title()
                val feed = Feed(Feed.DEFAULT_FEED_ID, title, baseUrl, Feed.DEDAULT_ICON_PATH, Feed.RSS_1, 0, siteUrl)
                return RssParseResult(feed)
            } else if (!document.getElementsByTag("rss").isEmpty()) {
                // RSS 2.0
                val links = document.getElementsByTag("link")
                var siteUrl = ""
                links.forEach { element ->
                    if (element.parent()?.tag().toString() == "channel") {
                        siteUrl = element.text()
                        return@forEach
                    }
                }
                if (siteUrl.isBlank()) {
                    siteUrl = url.protocol + "://" + url.host
                }
                val title = document.title()
                val feed = Feed(Feed.DEFAULT_FEED_ID, title, baseUrl, Feed.DEDAULT_ICON_PATH, Feed.RSS_2, 0, siteUrl)
                return RssParseResult(feed)
            } else if (!document.getElementsByTag("feed").isEmpty()) {
                // ATOM:
                //<?xml version="1.0" encoding="utf-8"?>
                //<feed xmlns="http://www.w3.org/2005/Atom">
                //   <title>Example Feed</title>
                //   <link href="http://example.org/"/>
                //    <updated>2003-12-13T18:30:02Z</updated>
                //    <author>
                //        <name>John Doe</name>
                //    </author>
                //    <id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>
                //    <entry>
                //        <title>Atom-Powered Robots Run Amok</title>
                //        <link href="http://example.org/2003/12/13/atom03"/>
                //        <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>
                //        <updated>2003-12-13T18:30:02Z</updated>
                //        <summary>Some text.</summary>
                //    </entry>
                //</feed>
                val links = document.getElementsByTag("link")
                val siteUrl = if (links.isEmpty()) {
                    url.protocol + "://" + url.host
                } else {
                    links[0].attr("href")
                }
                val title = document.title()
                val feed = Feed(Feed.DEFAULT_FEED_ID, title, baseUrl, Feed.DEDAULT_ICON_PATH, Feed.ATOM, 0, siteUrl)
                return RssParseResult(feed)
            } else if (!document.getElementsByTag("html").isEmpty()) {
                if (checkCanonical) {
                    // <link rel="canonical" href="http://xxxxxxxx">
                    document.getElementsByAttributeValue("rel", "canonical").run {
                        if (isEmpty()) return@run
                        // Canonical setting sets the actual site URL for google search
                        var pcUrl = this[0].attr("href")
                        if (!pcUrl.startsWith("http://") && !pcUrl.startsWith("https")) {
                            // Path only, add protocol and host
                            pcUrl = URL(url.protocol, url.host, pcUrl).toString()
                        }
                        Timber.d("canonical setting is found, try to parse $pcUrl")
                        return parse(pcUrl)
                    }
                }
                //<link rel="alternate" type="application/rss+xml" title="TechCrunch Japan &raquo; フィード" href="http://jp.techcrunch.com/feed/" />
                val elements = document.getElementsByAttributeValue("type", "application/rss+xml")
                if (elements.isEmpty()) {
                    val feedPathUrl = URL(url.protocol, url.host, "feed").toString()
                    return if (url.toString() == feedPathUrl) {
                        // Already check URL that path is "feed"
                        Timber.d("RSS URL was not found")
                        RssParseResult(failedReason = RssParseResult.FailedReason.NOT_FOUND)
                    } else {
                        parseRssXml(URL(url.protocol, url.host, "feed").toString(), false)
                    }
                }
                var feedUrl = elements[0].attr("href")
                if (feedUrl.startsWith("//")) {
                    // In http://smhn.info, feedUrl is "//smhn.info/feed"
                    // "//smhn.info" is not needed, get path from after URL host
                    val path = feedUrl.substring(2 + url.host.length)
                    feedUrl = URL(url.protocol, url.host, path).toString()
                } else if (!feedUrl.startsWith("http://") && !feedUrl.startsWith("https")) {
                    // Path only, add protocol and host
                    feedUrl = URL(url.protocol, url.host, feedUrl).toString()
                }
                Timber.d("RSS URL was found, $feedUrl")
                return parseRssXml(feedUrl, false)
            } else {
                Timber.d("Fail, not RSS")
            }
        } catch (e: MalformedURLException) {
            Timber.d("Fail, MalformedURLException")
            e.printStackTrace()
        } catch (e: IOException) {
            Timber.d("Fail, IOException")
            e.printStackTrace()
        } catch (e: Exception) {
            Timber.d("Fail, Exception")
            e.printStackTrace()
        }

        return RssParseResult(failedReason = RssParseResult.FailedReason.NOT_FOUND)
    }

    fun parseArticlesFromRss(inputStream: InputStream): ArrayList<Article> {
        val articles = ArrayList<Article>()

        // TODO Get hatena bookmark(?) count
        var article = Article(0, "", "", Article.UNREAD, Article.DEDAULT_HATENA_POINT, 0, 0, "", "")

        // Flag for not getting "Site's" title and url
        var itemFlag = false
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(inputStream, "UTF-8")

            // Start parse to the END_DOCUMENT
            var eventType = parser.eventType
            var tag = parser.name
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (tag) {
                            // when new Item found, initialize currentItem
                            "item", "entry" -> {
                                article = Article(0, "", "", Article.UNREAD, Article.DEDAULT_HATENA_POINT, 0, 0, "", "")
                                itemFlag = true
                            }
                            // add Title and Link to currentItem
                            "title" -> {
                                if (itemFlag && article.title == "") {
                                    val title = TextUtil.removeLineFeed(parser.nextText())
                                    article.title = title
                                }
                            }
                            "link" -> {
                                if (itemFlag && article.url.isBlank()) {
                                    // RSS 1.0 & 2.0
                                    val href = parseAtomAriticleUrl(parser)
                                    if (href.isNotBlank() && href != "\n") {
                                        // Atom
                                        article.url = href
                                    } else {
                                        val nextText = parser.nextText() ?: ""
                                        if (nextText.isNotBlank() && nextText != "\n") {
                                            article.url = nextText
                                        } else {
                                            try {
                                                article.url = parser.text
                                            } catch (ignored: IllegalStateException) {
                                            } catch (ignored: NullPointerException) {
                                            }
                                        }
                                    }
                                }
                            }
                            "date", "pubDate", "published", "updated" -> {
                                if (itemFlag && article.postedDate == 0L) {
                                    val date = parser.nextText()
                                    article.postedDate = DateParser.changeToJapaneseDate(date)
                                }
                            }
                        }
                    }

                    // When </Item> add currentItem to DB
                    XmlPullParser.END_TAG -> {
                        tag = parser.name
                        if (tag == "item" || tag == "entry") {
                            articles.add(article)
                            itemFlag = false
                        }
                    }
                }
                eventType = parser.next()
                tag = parser.name
                if (eventType == XmlPullParser.END_TAG && (tag == "rss" || tag == "rdf" || tag == "feed")) {
                    break
                }
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return articles
    }

    /**
     * Parse URL from link tag
     *
     * ATOM: <link rel='alternate' type='text/html' href='http://xxxx' title='yyyy'></link>
     *
     * @param parser Parser that is in <link></link>
     * @return URL or empty string
     */
    private fun parseAtomAriticleUrl(parser: XmlPullParser): String {
        var isTypeTextHtml = false
        var hasType = false
        var href = ""
        for (i in 0 until parser.attributeCount) {
            val attributeName = parser.getAttributeName(i)
            val attributeValue = parser.getAttributeValue(i)
            if (attributeName == "type") {
                hasType = true
                if (attributeValue == "text/html") {
                    isTypeTextHtml = true
                }
                continue
            }
            if (attributeName == "href") {
                if (attributeValue.startsWith("http://") || attributeValue.startsWith("https://")) {
                    href = attributeValue
                }
            }
        }
        return if (!hasType || isTypeTextHtml) href else ""
    }
}