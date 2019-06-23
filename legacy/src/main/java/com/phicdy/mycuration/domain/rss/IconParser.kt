package com.phicdy.mycuration.domain.rss

import org.jsoup.Jsoup
import java.net.URL

class IconParser {

    fun parseHtml(urlString: String): String {
        if (urlString.isBlank()) return ""
        try {
            Jsoup.connect(urlString).get().getElementsByTag("link").forEach { link ->
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}