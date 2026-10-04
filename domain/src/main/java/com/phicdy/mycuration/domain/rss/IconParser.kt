package com.phicdy.mycuration.domain.rss

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL

class IconParser {

    fun parseHtml(urlString: String): String {
        if (urlString.isBlank()) return ""
        try {
            return findIconUrl(Jsoup.connect(urlString).get(), urlString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    internal fun findIconUrl(document: Document, urlString: String): String {
        document.getElementsByTag("link").forEach { link ->
            if (link.attr("rel") != "shortcut icon" && link.attr("rel") != "apple-touch-icon") return@forEach
            val href = link.attr("href")
            if (!href.startsWith("http:") && !href.startsWith("https:")) {
                val url = URL(urlString)
                // The link is //<Host>/<path>
                return if (href.startsWith("//")) {
                    url.protocol + ":" + href
                } else URL(url.protocol, url.host, href).toString()
            }
            return href
        }
        return ""
    }
}
