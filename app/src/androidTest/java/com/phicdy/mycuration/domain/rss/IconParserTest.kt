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
        assertEquals("http://b.hatena.ne.jp/favicon.ico", parser.parseHtml("http://b.hatena.ne.jp"))
    }
}
