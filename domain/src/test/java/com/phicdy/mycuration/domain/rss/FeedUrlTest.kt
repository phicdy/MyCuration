package com.phicdy.mycuration.domain.rss

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.URL

class FeedUrlTest {

    @Test
    fun resolveReturnsAbsoluteHrefAsIs() {
        assertThat(URL("https://example.com/top").resolve("https://feeds.example.com/rss/"))
            .isEqualTo("https://feeds.example.com/rss/")
    }

    @Test
    fun resolveAddsProtocolAndHostToPathOnlyHref() {
        assertThat(URL("https://example.com/top/index.html").resolve("/feed"))
            .isEqualTo("https://example.com/feed")
    }

    @Test
    fun resolveAddsPathOnlyHrefToHostWithoutPath() {
        assertThat(URL("https://example.com").resolve("/feed"))
            .isEqualTo("https://example.com/feed")
    }

    @Test
    fun resolveAddsProtocolToProtocolRelativeHref() {
        assertThat(URL("http://smhn.info").resolve("//smhn.info/feed"))
            .isEqualTo("http://smhn.info/feed")
    }

    @Test
    fun resolveUsesHostOfProtocolRelativeHref() {
        assertThat(URL("https://example.com").resolve("//cdn.example.com/feed"))
            .isEqualTo("https://cdn.example.com/feed")
    }

    @Test
    fun resolveResolvesHrefRelativeToPagePath() {
        assertThat(URL("https://example.com/blog/index.html").resolve("feed"))
            .isEqualTo("https://example.com/blog/feed")
    }

    @Test
    fun resolveKeepsPortForPathOnlyHref() {
        assertThat(URL("http://localhost:8080/top").resolve("/feed"))
            .isEqualTo("http://localhost:8080/feed")
    }

    @Test
    fun resolveKeepsPortForProtocolRelativeHref() {
        assertThat(URL("http://localhost:8080/top").resolve("//localhost:8080/rss"))
            .isEqualTo("http://localhost:8080/rss")
    }

    @Test
    fun toSiteUrlReturnsProtocolAndHost() {
        assertThat(URL("https://example.com/feed/rss.xml?type=all").toSiteUrl())
            .isEqualTo("https://example.com")
    }

    @Test
    fun toSiteUrlKeepsPort() {
        assertThat(URL("http://localhost:8080/feed").toSiteUrl())
            .isEqualTo("http://localhost:8080")
    }
}
