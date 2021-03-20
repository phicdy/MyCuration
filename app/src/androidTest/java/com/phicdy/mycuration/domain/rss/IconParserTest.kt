package com.phicdy.mycuration.domain.rss

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class IconParserTest {

    @Test
    fun testParseXml() {
        val parser = IconParser()
        assertEquals("https://gigazine.net/favicon.ico", parser.parseHtml("https://gigazine.net"))
        assertEquals("https://jp.techcrunch.com/wp-content/themes/techcrunch-jp-2015/assets/images/TC_favicon.png", parser.parseHtml("http://jp.techcrunch.com"))
        assertEquals("http://b.hatena.ne.jp/favicon.ico", parser.parseHtml("http://b.hatena.ne.jp"))
    }
}
