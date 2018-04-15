package com.phicdy.mycuration.domain.rss

import com.phicdy.mycuration.data.rss.Article
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class RssParserTest {

    private lateinit var parser: RssParser
    @Before
    fun setup() {
        parser = RssParser()
    }

    @Test
    fun `parse RSS Version 1 and first size is correct`() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles.size, `is`(2))
    }

    @Test
    fun `parse RSS Version 1 and first title is correct`() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[0].title, `is`("トップレベルのコンピュータエンジニアなら普段からチェックして当然の技術系メディアN選 - kuenishi's blog"))
    }

    @Test
    fun `parse RSS Version 1 and first URL is correct`() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[0].url, `is`("https://kuenishi.hatenadiary.jp/entry/2018/04/13/022908"))
    }

    @Test
    fun `parse RSS Version 1 and first date is correct`() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[0].postedDate, `is`(1523554436000L))
    }

    @Test
    fun `parse RSS Version 1 and first status is unread`() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[0].status, `is`(Article.UNREAD))
    }

    @Test
    fun `parse RSS Version 1 and first hatena point is -1`() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[0].point, `is`("-1"))
    }

    @Test
    fun `parse RSS Version 1 and second title is correct`() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[1].title, `is`("「Excelが使える」のレベルを的確に見抜ける、入社試験に使えるサンプル問題が公開中【やじうまWatch】 - INTERNET Watch"))
    }

    @Test
    fun `parse RSS Version 1 and second URL is correct`() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[1].url, `is`("https://internet.watch.impress.co.jp/docs/yajiuma/1116878.html"))
    }

    @Test
    fun `parse RSS Version 1 and second date is correct`() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[1].postedDate, `is`(1523567035000L))
    }

    @Test
    fun `parse RSS Version 1 and second status is unread`() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[1].status, `is`(Article.UNREAD))
    }

    @Test
    fun `parse RSS Version 1 and second hatena point is -1`() {
        val articles = parser.parseXml(RssV1().text().byteInputStream(), -1)
        assertThat(articles[1].point, `is`("-1"))
    }

    @Test
    fun `parse RSS Version 2 and size is correct`() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles.size, `is`(2))
    }

    @Test
    fun `parse RSS Version 2 and first title is correct`() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[0].title, `is`("内閣支持率が続落し38% 時事"))
    }

    @Test
    fun `parse RSS Version 2 and first URL is correct`() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[0].url, `is`("https://news.yahoo.co.jp/pickup/6278905"))
    }

    @Test
    fun `parse RSS Version 2 and first date is correct`() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[0].postedDate, `is`(1523607277000L))
    }

    @Test
    fun `parse RSS Version 2 and first status is unread`() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[0].status, `is`(Article.UNREAD))
    }

    @Test
    fun `parse RSS Version 2 and first hatena point is -1`() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[0].point, `is`("-1"))
    }

    @Test
    fun `parse RSS Version 2 and second title is correct`() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[1].title, `is`("ハム球場で迷惑行為 県警警戒"))
    }

    @Test
    fun `parse RSS Version 2 and second URL is correct`() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[1].url, `is`("https://news.yahoo.co.jp/pickup/6278920"))
    }

    @Test
    fun `parse RSS Version 2 and second date is correct`() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[1].postedDate, `is`(1523612750000L))
    }

    @Test
    fun `parse RSS Version 2 and second status is unread`() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[1].status, `is`(Article.UNREAD))
    }

    @Test
    fun `parse RSS Version 2 and second hatena point is -1`() {
        val articles = parser.parseXml(RssV2().text().byteInputStream(), -1)
        assertThat(articles[1].point, `is`("-1"))
    }

}