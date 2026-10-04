package com.phicdy.mycuration.domain.rss

import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.junit.Test

class IconParserTest {

    private val parser = IconParser()

    private fun findIconUrl(head: String, url: String = "https://example.com"): String =
        parser.findIconUrl(Jsoup.parse("<html><head>$head</head><body></body></html>"), url)

    @Test
    fun returnsAbsoluteShortcutIcon() {
        assertThat(findIconUrl("""<link rel="shortcut icon" href="https://cdn.example.com/favicon.ico">"""))
            .isEqualTo("https://cdn.example.com/favicon.ico")
    }

    @Test
    fun resolvesRootRelativeShortcutIcon() {
        assertThat(findIconUrl("""<link rel="shortcut icon" href="/img/favicon.ico">""", "https://kindou.info"))
            .isEqualTo("https://kindou.info/img/favicon.ico")
    }

    @Test
    fun resolvesProtocolRelativeIcon() {
        assertThat(findIconUrl("""<link rel="shortcut icon" href="//static.example.com/favicon.ico">""", "http://example.com"))
            .isEqualTo("http://static.example.com/favicon.ico")
    }

    @Test
    fun returnsAppleTouchIcon() {
        assertThat(findIconUrl("""<link rel="apple-touch-icon" href="https://example.com/touch.png">"""))
            .isEqualTo("https://example.com/touch.png")
    }

    @Test
    fun returnsFirstMatchingIcon() {
        val head = """
            <link rel="stylesheet" href="/style.css">
            <link rel="apple-touch-icon" href="/touch.png">
            <link rel="shortcut icon" href="/favicon.ico">
        """
        assertThat(findIconUrl(head)).isEqualTo("https://example.com/touch.png")
    }

    @Test
    fun returnsEmptyWhenNoIconLink() {
        assertThat(findIconUrl("""<link rel="stylesheet" href="/style.css">""")).isEmpty()
    }

    @Test
    fun returnsEmptyForBlankUrl() {
        assertThat(parser.parseHtml("")).isEmpty()
    }
}
